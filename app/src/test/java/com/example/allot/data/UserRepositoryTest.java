package com.example.allot.data;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
public class UserRepositoryTest {
    /**
     * Performs build cleanup operations deletes organizer events and removes user from others.
     */
    @Test
    public void buildCleanupOperations_deletesOrganizerEventsAndRemovesUserFromOthers() {
        List<UserRepository.EventCleanupTarget> cleanupTargets = new ArrayList<>();
        cleanupTargets.add(new UserRepository.EventCleanupTarget("events/event-1", "device-1"));
        cleanupTargets.add(new UserRepository.EventCleanupTarget("events/event-2", "other-organizer"));

        List<UserRepository.CleanupOperation> operations = UserRepository.buildCleanupOperations("device-1", cleanupTargets);

        assertEquals(2, operations.size());
        assertEquals(UserRepository.CleanupOperation.Type.DELETE_EVENT, operations.get(0).getType());
        assertEquals("events/event-1", operations.get(0).getDocumentPath());
        assertEquals(UserRepository.CleanupOperation.Type.REMOVE_USER_FROM_EVENT, operations.get(1).getType());
        assertEquals("events/event-2", operations.get(1).getDocumentPath());
        assertEquals("device-1", operations.get(1).getDeviceId());
    }

    /**
     * Performs chunk cleanup operations splits large operation lists into firestore sized batches.
     */
    @Test
    public void chunkCleanupOperations_splitsLargeOperationListsIntoFirestoreSizedBatches() {
        List<UserRepository.CleanupOperation> operations = new ArrayList<>();
        for (int i = 0; i < 501; i++) {
            operations.add(UserRepository.CleanupOperation.removeUserFromEvent("events/event-" + i, "device-1"));
        }
        operations.add(UserRepository.CleanupOperation.deleteUser("device-1"));

        List<List<UserRepository.CleanupOperation>> batches = UserRepository.chunkCleanupOperations(operations);

        assertEquals(2, batches.size());
        assertEquals(UserRepository.MAX_BATCH_OPERATIONS, batches.get(0).size());
        assertEquals(2, batches.get(1).size());
        assertEquals(UserRepository.CleanupOperation.Type.DELETE_USER,
                batches.get(1).get(batches.get(1).size() - 1).getType());
    }
}
