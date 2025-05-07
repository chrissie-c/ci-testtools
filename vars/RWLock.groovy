import org.jenkinsci.plugins.lockable_resources-plugin.LockableResourcesManager
import org.jenkinsci.plugins.lockable_resources-plugin.locks.LockMode
import org.jenkinsci.plugins.lockable_resources-plugin.queue.QueuedContext

/**
 * Provides read/write locking functionality for Jenkins jobs using the Lockable Resources plugin.
 */
class RWLock {
    final String lockName
    final String mode
    final Map<String, Boolean> flags = [:]
    QueuedContext context = null

    /**
     * Constructor for the RWLock.
     * @param lockName The name of the lockable resource. Ensure this name is consistent across jobs.
     * @param mode The desired lock mode: 'read' or 'write'.
     * @param flags A map of optional flags (currently not used in this basic implementation).
     * @throws IllegalArgumentException if the mode is invalid.
     */
    RWLock(String lockName, String mode, Map<String, Boolean> flags = [:]) {
        if (!['read', 'write'].contains(mode.toLowerCase())) {
            throw new IllegalArgumentException("Invalid lock mode: ${mode}. Must be 'read' or 'write'.")
        }
        this.lockName = lockName
        this.mode = mode.toLowerCase()
        this.flags.putAll(flags)
    }

    /**
     * Acquires the lock based on the specified mode.
     * @return true if the lock was acquired successfully, false otherwise (if non-blocking).
     * @throws Exception if there's an issue acquiring the lock.
     */
    boolean acquire() {
        LockableResourcesManager lrm = LockableResourcesManager.get()
        def resource = lrm.fromName(lockName)

        if (!resource) {
            // Automatically create the resource if it doesn't exist
            try {
                lrm.create(lockName, "", null) // Description can be empty, labels null for simplicity
                resource = lrm.fromName(lockName)
                println "Created Lockable Resource: ${lockName}"
            } catch (Exception e) {
                println "Error creating Lockable Resource '${lockName}': ${e.getMessage()}"
                throw e
            }
        }

        LockMode lockMode = (mode == 'write') ? LockMode.EXCLUSIVE : LockMode.SHARED

        try {
            context = lrm.queue(resource.name, 0, lockMode, null) // 0 timeout means wait indefinitely
            context.acquire()
            println "Acquired ${mode} lock for resource: ${lockName}"
            return true
        } catch (Exception e) {
            println "Error acquiring ${mode} lock for resource '${lockName}': ${e.getMessage()}"
            return false // Or potentially re-throw depending on desired behavior
        }
    }

    /**
     * Acquires the lock with a specified timeout.
     * @param timeoutSeconds The maximum time to wait for the lock in seconds.
     * @return true if the lock was acquired successfully within the timeout, false otherwise.
     * @throws Exception if there's an issue acquiring the lock.
     */
    boolean acquire(int timeoutSeconds) {
        LockableResourcesManager lrm = LockableResourcesManager.get()
        def resource = lrm.fromName(lockName)

        if (!resource) {
            try {
                lrm.create(lockName, "", null)
                resource = lrm.fromName(lockName)
                println "Created Lockable Resource: ${lockName}"
            } catch (Exception e) {
                println "Error creating Lockable Resource '${lockName}': ${e.getMessage()}"
                throw e
            }
        }

        LockMode lockMode = (mode == 'write') ? LockMode.EXCLUSIVE : LockMode.SHARED

        try {
            context = lrm.queue(resource.name, timeoutSeconds, lockMode, null)
            boolean acquired = context.acquire()
            if (acquired) {
                println "Acquired ${mode} lock for resource: ${lockName}"
            } else {
                println "Failed to acquire ${mode} lock for resource: ${lockName} within ${timeoutSeconds} seconds."
            }
            return acquired
        } catch (Exception e) {
            println "Error acquiring ${mode} lock for resource '${lockName}': ${e.getMessage()}"
            return false
        }
    }

    /**
     * Releases the acquired lock.
     */
    void release() {
        if (context) {
            context.release()
            println "Released ${mode} lock for resource: ${lockName}"
            context = null
        } else {
            println "Warning: Attempted to release lock '${lockName}' but it was not acquired by this instance."
        }
    }
}
