package uk.ac.bbsrc.tgac.miso.sqlstore;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.CascadeType;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.jdbc.core.JdbcTemplate;

import uk.ac.bbsrc.tgac.miso.AbstractDAOTest;
import uk.ac.bbsrc.tgac.miso.core.data.AbstractQC;
import uk.ac.bbsrc.tgac.miso.core.data.Partition;
import uk.ac.bbsrc.tgac.miso.core.data.RunQC;
import uk.ac.bbsrc.tgac.miso.core.data.SequencerPartitionContainer;
import uk.ac.bbsrc.tgac.miso.core.data.SequencerPoolPartition;
import uk.ac.bbsrc.tgac.miso.core.data.impl.PartitionImpl;
import uk.ac.bbsrc.tgac.miso.core.data.impl.RunImpl;
import uk.ac.bbsrc.tgac.miso.core.data.impl.RunQCImpl;
import uk.ac.bbsrc.tgac.miso.core.data.impl.SequencerPartitionContainerImpl;
import uk.ac.bbsrc.tgac.miso.core.data.type.QcType;
import uk.ac.bbsrc.tgac.miso.core.exception.MisoNamingException;
import uk.ac.bbsrc.tgac.miso.core.factory.TgacDataObjectFactory;

public class SQLRunQCDAOTest extends AbstractDAOTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @InjectMocks
  private SQLRunQCDAO dao;

  @Mock
  private SQLRunDAO runDAO;
  @Mock
  private SQLSequencerPartitionContainerDAO sequencerPartitionContainerDAO;

  @Autowired
  @Spy
  private JdbcTemplate jdbcTemplate;


  @Before
  public void setup() throws IOException, MisoNamingException {
    MockitoAnnotations.initMocks(this);
    dao.setJdbcTemplate(jdbcTemplate);
    dao.setDataObjectFactory(new TgacDataObjectFactory());

    SequencerPartitionContainer spp = new SequencerPartitionContainerImpl();
    List<Partition> partitionList = new ArrayList();
    Partition partition = new PartitionImpl();
    partition.setPartitionNumber(new Integer(3));
    partitionList.add(partition);
    spp.setPartitions(partitionList);
    when(sequencerPartitionContainerDAO.lazyGet(anyLong())).thenReturn( spp);
  }

  @Test
  public void testSave() throws Exception {
    RunQC runQC = new RunQCImpl();
    runQC.setId(AbstractQC.UNSAVED_ID);
    runQC.setQcCreator("creator");
    runQC.setDoNotProcess(true);
    runQC.setInformation("information");
    runQC.setQcDate(new Date());
    runQC.setRun(new RunImpl());
    runQC.setQcType(new QcType());
    runQC.setPartitionSelections(new ArrayList<Partition>());
    long saveId = dao.save(runQC);
    RunQC retrievedQC = dao.get(saveId);
    assertEquals(runQC.getDoNotProcess(), retrievedQC.getDoNotProcess());
    assertEquals(runQC.getInformation(), retrievedQC.getInformation());

    DateFormatter df = new DateFormatter("yyyy-MM-dd");
    df.print(runQC.getQcDate(), Locale.CANADA);
    assertEquals(df.print(runQC.getQcDate(), Locale.CANADA), df.print(retrievedQC.getQcDate(), Locale.CANADA));
    assertEquals(runQC.getQcCreator(), retrievedQC.getQcCreator());
  }

  @Test
  public void testGet() throws Exception {
    RunQC runQC = dao.get(2);
    assertEquals(2, runQC.getId());
    assertEquals("information2", runQC.getInformation());
    assertEquals("username2", runQC.getQcCreator());
    assertFalse(runQC.getDoNotProcess());
  }

  @Test
  public void testLazyGet() throws Exception {
    RunQC runQC = dao.lazyGet(3);
    assertEquals(3, runQC.getId());
    assertEquals("information3", runQC.getInformation());
    assertEquals("username3", runQC.getQcCreator());
    assertTrue(runQC.getDoNotProcess());
  }

  @Test
  public void testListByRunId() throws Exception {
    Collection<RunQC> runQCs = dao.listByRunId(2);
    RunQC runQC = runQCs.iterator().next();
    assertEquals(1, runQCs.size());
    assertEquals(2, runQC.getId());
    assertEquals("information2", runQC.getInformation());
    assertEquals("username2", runQC.getQcCreator());
    assertFalse(runQC.getDoNotProcess());
  }

  @Test
  public void testListAll() throws Exception {
    Collection<RunQC> runQCs = dao.listAll();
    assertTrue(runQCs.size() == 3);
    assertTrue(runQCs.contains(dao.get(1)));
    assertTrue(runQCs.contains(dao.get(2)));
    assertTrue(runQCs.contains(dao.get(3)));
  }

  @Test
  public void testCount() throws Exception {
    int count = dao.count();
    assertEquals(3, count);
  }

  @Test
  public void testListPartitionSelectionsByRunQcId() throws Exception {
    Collection<Partition> partitions = dao.listPartitionSelectionsByRunQcId(1);
    assertEquals(1, partitions.size());
    assertEquals(new Integer(3), partitions.iterator().next().getPartitionNumber());
  }

  @Test
  public void testRemove() throws Exception {
    dao.setCascadeType(CascadeType.ALL);
    RunQC runQC = dao.get(3);
    boolean remove = dao.remove(runQC);
    assertTrue(remove);
    assertNull(dao.get(3));
  }

  @Test
  public void testListAllRunQcTypes() throws Exception {
    Collection<QcType> qcTypes = dao.listAllRunQcTypes();
    assertEquals(2, qcTypes.size());
  }

  @Test
  public void testGetRunQcTypeById() throws Exception {
    QcType runQcTypeById = dao.getRunQcTypeById(6);
    assertNotNull(runQcTypeById);
    assertEquals("SeqInfo QC", runQcTypeById.getName());
    assertEquals("Post-run completion run QC step, undertaken by the SeqInfo team, as part of the primary analysis stage.", runQcTypeById.getDescription());
  }

  @Test
  public void testGetRunQcTypeByName() throws Exception {
    QcType runQcTypeByName = dao.getRunQcTypeByName("SeqOps QC");
    assertNotNull(runQcTypeByName);
    assertTrue(5 == runQcTypeByName.getQcTypeId());
    assertEquals("Post-run completion run QC step, undertaken by the SeqOps team, to move a run through to the primary analysis stage.", runQcTypeByName.getDescription());
  }
}