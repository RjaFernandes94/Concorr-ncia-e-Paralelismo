package cp.benchmark.intset;

import java.util.Random;

/**
 * @author Pascal Felber
 * @since 0.1
 */
public class BenchmarkThread extends cp.benchmark.BenchmarkThread {
  private final IntSet m_set;
  private final int m_range;
  int m_nb_add;
  int m_nb_remove;
  int m_nb_contains;
  private final int m_rate;
  boolean m_write;
  int m_last;
  private final Random m_random;
  final int[] m_checker;

  public BenchmarkThread(IntSet set, int range, int rate) {
    m_set = set;
    m_range = range;
    m_nb_add = m_nb_remove = m_nb_contains = 0;
    m_rate = rate;
    m_write = true;
    m_random = new Random();
    m_checker = new int[range];
  }

  protected void step(int phase) {
    int i = m_random.nextInt(100);
    if (i < m_rate) {
      if (m_write) {
        m_last = m_random.nextInt(m_range);
        if (m_set.add(m_last)) {
          m_write = false;
          m_checker[m_last]++;
        }
        if (phase == Benchmark.TEST_PHASE) m_nb_add++;
      } else {
        if (m_set.remove(m_last)) m_checker[m_last]--;
        if (phase == Benchmark.TEST_PHASE) m_nb_remove++;
        m_write = true;
      }
    } else {
      m_set.contains(m_random.nextInt(m_range));
      if (phase == Benchmark.TEST_PHASE) m_nb_contains++;
    }
  }

  public String getStats() {
    return "A=" + m_nb_add + ", R=" + m_nb_remove + ", C=" + m_nb_contains;
  }
}
