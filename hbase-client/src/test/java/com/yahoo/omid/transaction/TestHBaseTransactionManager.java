package com.yahoo.omid.transaction;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.yahoo.omid.tsoclient.TSOClient;

@Test(groups = "sharedHBase")
public class TestHBaseTransactionManager extends OmidTestBase {

    private static final long FAKE_EPOCH = 3L;

    @Test
    public void testTxManagerGetsTimestampsInTheRightEpoch(ITestContext context) throws Exception {

        TSOClient tsoClient = spy(getClient(context));

        // Modify the epoch before testing the begin method
        doReturn(FAKE_EPOCH).when(tsoClient).getEpoch();

        AbstractTransactionManager tm = spy((AbstractTransactionManager) newTransactionManager(context, tsoClient));

        // Create a transaction with the initial setup and check that
        Transaction tx1 = tm.begin();
        assertEquals(3, tx1.getTransactionId());
        verify(tsoClient, timeout(100).times(3)).getEpoch();

    }

}
