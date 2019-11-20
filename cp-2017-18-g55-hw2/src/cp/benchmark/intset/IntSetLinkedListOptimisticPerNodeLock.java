package cp.benchmark.intset;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Pascal Felber
 * @author Tiago Vale
 * @since 0.1
 */
public class IntSetLinkedListOptimisticPerNodeLock implements IntSet {

  public class Node {
    private final int m_value;
    private Node m_next;
    private ReentrantLock lock;
    private int add;
    private int remove;

    public Node(int value, Node next) {
      m_value = value;
      m_next = next;
      lock = new ReentrantLock();
      add=0;
      remove=0;
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
    
    public void incAdd() {
    	add++;
    }
    
    public void incAmountAdd(int amount) {
    	add = add + amount;
    }
    
    public void incAmountRemove(int amount) {
    	remove = remove + amount;
    }
    
    public void incRemove() {
    	remove++;
    }
    
    public int getAdd() {
    	return add;
    }
    
    public int getRemove() {
    	return remove;
    }
    
    public void lockNode() {
    	lock.lock();
    }
    
    public void unlockNode() {
    	lock.unlock();
    }
  }

  private final Node m_first;

  public IntSetLinkedListOptimisticPerNodeLock() {
    Node min = new Node(Integer.MIN_VALUE);
    Node max = new Node(Integer.MAX_VALUE);
    min.setNext(max);
    m_first = min;
  }

  public boolean add(int value) {
	while(true) {
		Node previous = m_first;
	    Node next = previous.getNext();
	    
	    int v;
	    while ((v = next.getValue()) < value) {
	      previous = next;
	      next = previous.getNext();
	    }
	    
	    previous.lockNode();
	    next.lockNode();
	    
	    try {
	    	if(validateLogic(previous, next)) {
	    		if(value == v)
	    			return false;
	    		else {
	    			previous.setNext(new Node(value, next));
	    			previous.incAdd();
	    			return true;
	    		}
	    	}
	    } finally {
	    	previous.unlockNode();
		    next.unlockNode();
	    }
	}
  }

  public boolean remove(int value) {
	while(true) {
		Node previous = m_first;
	    Node next = previous.getNext();
	    
	    int v;
	    while ((v = next.getValue()) < value) {
	      previous = next;
	      next = previous.getNext();
	    }
	    
	    previous.lockNode();
	    next.lockNode();
	    
	    try {
	    	if(validateLogic(previous, next)) {
	    		if(value == v) {
	    			previous.incAmountAdd(next.getAdd());
    				previous.incAmountRemove(next.getRemove());
	    			previous.setNext(next.getNext());
	    			previous.incRemove();
	    			return true;
	    		} else return false;
	    	}
	    } finally {
	    	previous.unlockNode();
		    next.unlockNode();
	    }
	}
  }

  public boolean contains(int value) {  
	while(true) {
		Node previous = m_first;
	    Node next = previous.getNext();
	    int v;
	    
	    while ((v = next.getValue()) < value) {
	      previous = next;
	      next = previous.getNext();
	    }
	    
	    try {
	    	previous.lockNode();
	    	next.lockNode();
	    	
	    	if(validateLogic(previous, next)) {
	    		if(value == v)
	    			return true;
	    		else return false;
	    	}
	    } finally {
	    	previous.unlockNode();
	    	next.unlockNode();
	    }
	}
  }
  
  public void validate() {
    java.util.Set<Integer> checker = new java.util.HashSet<>();
    int totalSize=2;
    int initialSize=2;
	int totalAdds=m_first.getAdd();
	int totalRemoves=m_first.getRemove();
    int previous_value = m_first.getValue();
    Node node = m_first.getNext();
    int value = node.getValue();
    while (value < Integer.MAX_VALUE) {
    	totalAdds= totalAdds + node.getAdd();
    	totalRemoves = totalRemoves + node.getRemove();
      assert previous_value < value : "list is unordered: " + previous_value + " before " + value;
      assert !checker.contains(value) : "list has duplicates: " + value;
      checker.add(value);
      previous_value = value;
      node = node.getNext();
      value = node.getValue();
      totalSize++;
    }
    totalAdds = totalAdds + node.getAdd();
    totalRemoves = totalRemoves + node.getRemove();
    assert (initialSize + totalAdds - totalRemoves) == totalSize : "list has a total size of " +totalSize+" but it should be "+ (initialSize + totalAdds - totalRemoves);
  }
  
  private boolean validateLogic(Node previous, Node next) {
	  Node node = m_first;
	  
	  while(node.getValue() <= previous.getValue()) {
		  if(node==previous && node.getNext()==next)
			  return true;
		  node = node.getNext();	  
	  }
	  
	  return false;
  }
}
