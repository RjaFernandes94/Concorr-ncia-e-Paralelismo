package cp.benchmark.intset;

import java.util.Random;

/**
 * @author Pascal Felber
 * @author Tiago Vale
 * @since 0.1
 */
public class Benchmark implements cp.benchmark.Benchmark {

  IntSet m_set;
  int m_range = 1 << 16;
  int m_rate = 20;
  int[] m_checker;

  public void init(String[] args) {
    boolean error = false;
    int initial = 256;
    if (args.length > 0) {
      if (args[0].equals("LinkedList")) m_set = new IntSetLinkedList();
      else if (args[0].equals("Synchronized")) m_set = new IntSetLinkedListSynchronized();
      else if (args[0].equals("GlobalLock")) m_set = new IntSetLinkedListGlobalLock();
      else if (args[0].equals("GlobalRWLock")) m_set = new IntSetLinkedListGlobalRWLock();
      else if (args[0].equals("PerNodeLock")) m_set = new IntSetLinkedListPerNodeLock();
      else if (args[0].equals("OptimisticPerNodeLock")) m_set = new IntSetLinkedListOptimisticPerNodeLock();
      else if (args[0].equals("LazyPerNodeLock")) m_set = new IntSetLinkedListLazyPerNodeLock();
      else if (args[0].equals("LockFree")) m_set = new IntSetLinkedListLockFree();
      else error = true;
    } else error = true;
    for (int i = 1; i < args.length && !error; i++) {
      if (args[i].equals("-i")) {
        if (++i < args.length) initial = Integer.parseInt(args[i]);
        else error = true;
      } else if (args[i].equals("-r")) {
        if (++i < args.length) m_range = Integer.parseInt(args[i]);
        else error = true;
      } else if (args[i].equals("-w")) {
        if (++i < args.length) m_rate = Integer.parseInt(args[i]);
        else error = true;
      } else error = true;
    }
    if (error) {
      System.out.println(
          "Benchmark arguments: (LinkedList|Synchronized|GlobalLock|GlobalRWLock|PerNodeLock|OptimisticPerNodeLock|LazyPerNodeLock|LockFree) [-i initial-size] [-r range] [-w write-rate]");
      System.exit(1);
    }
    Random random = new Random();
    m_checker = new int[m_range];
    for (int i = 0; i < initial; i++) {
      int elem = random.nextInt(m_range);
      if (m_set.add(elem)) m_checker[elem]++;
    }
    System.out.println("Initial size        = " + initial);
    System.out.println("Range               = " + m_range);
    System.out.println("Write rate          = " + m_rate + "%");
    System.out.println("List implementation = " + m_set.getClass().getSimpleName());
    System.out.println();
  }

  public cp.benchmark.BenchmarkThread createThread(int i, int nb) {
    return new BenchmarkThread(m_set, m_range, m_rate);
  }

  public String getStats(cp.benchmark.BenchmarkThread[] threads) {
    int add = 0;
    int remove = 0;
    int contains = 0;
    for (int i = 0; i < threads.length; i++) {
      add += ((BenchmarkThread) threads[i]).m_nb_add;
      remove += ((BenchmarkThread) threads[i]).m_nb_remove;
      contains += ((BenchmarkThread) threads[i]).m_nb_contains;
    }
    return "A=" + add + ", R=" + remove + ", C=" + contains;
  }

  public void validate(cp.benchmark.BenchmarkThread[] threads) {
    m_set.validate();
    
    for (int i = 0; i < threads.length; i++) {
    	
      int[] thread_checker = ((BenchmarkThread) threads[i]).m_checker;
      for (int j = 0; j < m_range; j++) {
        m_checker[j] += thread_checker[j];
      }
    }
    for (int i = 0; i < m_range; i++) {
      assert m_checker[i] == 0 || m_checker[i] == 1;
      if (m_checker[i] == 0) assert !m_set.contains(i) : i + " shouldn't exist in list";
      else assert m_set.contains(i) : i + " should exist in list";
    } 
  }
}
