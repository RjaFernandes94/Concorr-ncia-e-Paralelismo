package cp.benchmark.intset;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Pascal Felber
 * @author Tiago Vale
 * @since 0.1
 */
public class IntSetLinkedListGlobalLock implements IntSet {

  public class Node {
    private final int m_value;
    private Node m_next;

    public Node(int value, Node next) {
      m_value = value;
      m_next = next;
    }

    public Node(int value) {
      this(value, null);
    }

    public int getValue() {
      return m_value;
    }

    public void setNext(Node next) {
      m_next = next;
    }

    public Node getNext() {
      return m_next;
    }
  }

  private final Node m_first;
  private Lock lock;
  private int totalAdds;
  private int totalRemoves;

  public IntSetLinkedListGlobalLock() {
    Node min = new Node(Integer.MIN_VALUE);
    Node max = new Node(Integer.MAX_VALUE);
    min.setNext(max);
    m_first = min;
    lock= new ReentrantLock();
    totalAdds=0;
    totalRemoves=0;
  }

  public boolean add(int value) {
	lock.lock();
	try {
		Node previous = m_first;
		Node next = previous.getNext();
		int v;
		
		while ((v = next.getValue()) < value) {
			previous = next;
			next = previous.getNext();
		}

		if(value == v)
			return false;
		else {
			previous.setNext(new Node(value, next));
			totalAdds++;
			return true;
		}
	} finally {
		lock.unlock();
	}
  }

  public boolean remove(int value) {
	lock.lock();
	  
    try {
    	Node previous = m_first;
    	Node next = previous.getNext();
    	int v;
    	
    	while ((v = next.getValue()) < value) {
    		previous = next;
    		next = previous.getNext();
    	}
    	
    	if(value == v) {
    		previous.setNext(next.getNext());
    		totalRemoves++;
			return true;
    	} else return false;
    } finally {
		lock.unlock();
	}
  }

  public boolean contains(int value) {  
	lock.lock();
	try {
		Node previous = m_first;
		Node next = previous.getNext();
		int v;
		
		while ((v = next.getValue()) < value) {
			previous = next;
			next = previous.getNext();
		}
		
		if(value == v)
			return true;
		else return false;
	} finally {
		lock.unlock();
	}
  }

  public void validate() {
	  int totalSize=2;
	  int initialSize=2;
    java.util.Set<Integer> checker = new java.util.HashSet<>();
    int previous_value = m_first.getValue();
    Node node = m_first.getNext();
    int value = node.getValue();
    while (value < Integer.MAX_VALUE) {
      assert previous_value < value : "list is unordered: " + previous_value + " before " + value;
      assert !checker.contains(value) : "list has duplicates: " + value;
      checker.add(value);
      previous_value = value;
      node = node.getNext();
      value = node.getValue();
      totalSize++;
    }
    assert (initialSize + totalAdds - totalRemoves) == totalSize : "list has a total size of " +totalSize+" but it should be "+ (initialSize + totalAdds - totalRemoves); 
  }
}
