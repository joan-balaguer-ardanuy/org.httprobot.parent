package net.jxta.impl.cm;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.test.util.FileSystemTest;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public abstract class AbstractSrdiIndexBackendConcurrencyTest {
	
    private JUnit4Mockery mockContext = new JUnit4Mockery();
    
	private static final int NUM_INDICES = 8;
	private static final int NUM_GROUPS = 8;
	private static final int OPS_PER_INDEX = 1000;
	private static final long TEST_TIMEOUT = 120L;
	
	private File storeRoot;
	
	@Before
	public void setUp() throws Exception {
		storeRoot = FileSystemTest.createTempDirectory("SrdiIndexBackendConcurrencyTest");
	}
	
	@After
	public void tearDown() throws Exception {
		FileSystemTest.deleteDir(storeRoot);
	}
	
	@Test
	public void testSeparateIndexConcurrentSafety() throws Exception {
		PeerGroup group = createGroup(PeerGroupID.defaultNetPeerGroupID, "group1");
		Srdi[] indices = new Srdi[NUM_INDICES];
		
		for(int i=0; i < NUM_INDICES; i++) {
			indices[i] = new Srdi(createBackend(group, "index" + i), Srdi.NO_AUTO_GC);
		}
		
		randomLoadTest(indices);
	}

	private void randomLoadTest(Srdi[] indices) throws InterruptedException {
		CountDownLatch completionLatch = new CountDownLatch(indices.length);
		IndexRandomLoadTester[] testers = new IndexRandomLoadTester[indices.length];
		
		try {
			for(int i=0; i < indices.length; i++) {
				testers[i] = new IndexRandomLoadTester(indices[i], OPS_PER_INDEX, completionLatch);
				new Thread(testers[i]).start();
			}
			
			Assert.assertTrue("Timed out waiting for thread completion", completionLatch.await(TEST_TIMEOUT, TimeUnit.SECONDS));
			
			for(int i=0; i < indices.length; i++) {
				Assert.assertTrue(testers[i].isSuccessful());
			}
		} finally {
			for(int i=0; i < indices.length; i++) {
				if(indices[i] != null) {
					indices[i].stop();
				}
			}
		}
	}
	
	@Test
	public void testSeparateGroupConcurrentSafety() throws Exception {
		Srdi[] indices = new Srdi[NUM_INDICES * NUM_GROUPS];
		for(int groupNum = 0; groupNum < NUM_GROUPS; groupNum++) {
			PeerGroup group = createGroup(IDFactory.newPeerGroupID(), "group" + groupNum);
			for(int indexNum = 0; indexNum < NUM_INDICES; indexNum++) {
				indices[NUM_INDICES * groupNum + indexNum] = new Srdi(createBackend(group, "index" + indexNum), Srdi.NO_AUTO_GC);
			}
		}
		
		randomLoadTest(indices);
	}
	
	private PeerGroup createGroup(final PeerGroupID groupId, final String name) {
		final PeerGroup group = mockContext.mock(PeerGroup.class, name);
		mockContext.checking(new Expectations() {{
			ignoring(group).getStoreHome(); will(returnValue(storeRoot.toURI()));
			ignoring(group).getPeerGroupName(); will(returnValue(name));
			ignoring(group).getPeerGroupID(); will(returnValue(groupId));
			ignoring(group).getHomeThreadGroup(); will(returnValue(Thread.currentThread().getThreadGroup()));
		}});
		
		return group;
	}

	protected abstract SrdiAPI createBackend(PeerGroup group, String indexName) throws IOException;
}
