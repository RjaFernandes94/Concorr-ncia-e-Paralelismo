package cp.benchmark.intset;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Pascal Felber
 * @author Tiago Vale
 * @since 0.1
 */
public class IntSetLinkedListLockFree implements IntSet {

  public class Node {
    private final int m_value;
    private AtomicMarkableReference<Node> m_next;

    public Node(int value, AtomicMarkableReference<Node> next) {
      m_value = value;
      m_next = next;
    }

    public Node(int value) {
      this(value, null);
    }

    public int getValue() {
      return m_value;
    }

    public void setNext(AtomicMarkableReference<Node> next) {
      m_next = next;
    }

    public AtomicMarkableReference<Node> getNext() {
      return m_next;
    }
  }
  
  public class Window {
	  private Node previous;
	  private Node next;
	  
	  public Window(Node myPrev, Node myNext) {
	      previous = myPrev;
	      next = myNext;
	  }
	  
	  public Node getPrevious() {
		  return previous;
	  }
	  
	  public Node getNext() {
		  return next;
	  }
  }

  private final Node m_first;
  private AtomicInteger adds;
  private AtomicInteger rems;

  public IntSetLinkedListLockFree() {
    Node min = new Node(Integer.MIN_VALUE);
    Node max = new Node(Integer.MAX_VALUE);
    min.setNext(new AtomicMarkableReference<Node>(max, false));
    m_first = min;
    adds = new AtomicInteger();
    rems = new AtomicInteger();
  }

  public boolean add(int value) {
	while(true) {
		Window window = find(m_first, value);
		Node previous = window.getPrevious();
		Node next = window.getNext();
		
		//if(next.getValue()==Integer.MAX_VALUE) //se a lista so tiver os dois nos sentinela
			//return false;
		
		if(next.getValue() == value)
			return false;
		else {
			Node node = new Node(value);
			node.setNext(new AtomicMarkableReference<Node>(next, false));
			if(previous.getNext().compareAndSet(next, node, false, false)) {
				adds.getAndIncrement();
				return true;
			}
		}
	}
  }

  public boolean remove(int value) {
	boolean snip;
	
	while(true) {
		Window window = find(m_first, value);
		Node previous = window.getPrevious();
		Node next = window.getNext();
		
		if(next.getValue() == value) {
			Node succ = next.getNext().getReference();
			snip = next.getNext().attemptMark(succ, true);
			if(!snip)
				continue;
			
			rems.getAndIncrement();
			
			previous.getNext().compareAndSet(next, succ, false, false);
			
			return true;
		} else return false;
	}
  }

  public boolean contains(int value) {
	boolean[] marked = {false};
	Node next = m_first;
	
	while(next.getValue() < value)
		next = next.getNext().get(marked);
	
	return (next.getValue()==value && !marked[0]);
  }

  public void validate() {
    java.util.Set<Integer> checker = new java.util.HashSet<>();
    int previous_value = m_first.getValue();
    
    int totalSize=0;
    
    boolean[] marked = {false};
    Node node = m_first.getNext().get(marked);
    while(marked[0]) //se tiver marcado, nao esta na lista (logically removed)
    	node = node.getNext().get(marked);
    
    int value = node.getValue();
    while (value < Integer.MAX_VALUE) {
      totalSize++;
      assert previous_value < value : "list is unordered: " + previous_value + " before " + value;
      assert !checker.contains(value) : "list has duplicates: " + value;
      checker.add(value);
      previous_value = value;
      
      node = node.getNext().get(marked);
      while(marked[0]) //se tiver marcado, nao esta na lista (logically removed)
      	node = node.getNext().get(marked);
      
      value = node.getValue();
    }
    assert (adds.get() - rems.get()) == totalSize : "list has a total size of " +totalSize+" but it should be "+ (adds.get() - rems.get());
  }
  
  private Window find(Node head, int value) {
	  Node previous = null;
	  Node curr = null;
	  Node succ = null;
	  boolean[] marked = {false};
	  boolean snip;
	  
	  retry: while(true) {
		  previous = head;
		  curr = previous.getNext().getReference();
		  
		  //System.out.println("previous: " + previous.getValue());
		  //System.out.println("next: " + next.getValue());
		  //System.out.println("succ null? " + next.getNext()==null);
		  
		  while(true) {
			  if(isLastNode(curr)) //verificar se o next e o utlimo no da lista (no sentinela) antes de fazer um getNext do elemento
				  return new Window(previous, curr);
			  
			  succ = curr.getNext().get(marked);
			  
			  while(marked[0]) {
				  snip = previous.getNext().compareAndSet(curr, succ, false, false);
				  if(!snip)
					  continue retry;
				  
				  //int currAdds = curr.getAdd();
				  //int currRems = curr.getRemove();
				  //previous.incAddsRemoves(currAdds, currRems);
				  
				  if(isLastNode(succ)) //verificar se o succ e o utlimo no da lista (no sentinela) antes de fazer um getNext do elemento
					  return new Window(curr, succ);
				  
				  curr = succ;
				  succ = curr.getNext().get(marked);
			  }
			  
			  if(curr.getValue() >= value)
				  return new Window(previous, curr);
			  
			  previous = curr;
			  curr = succ;
		  }
	  }
  }
  
  private boolean isLastNode(Node node) {
	  return (node.getValue() == Integer.MAX_VALUE);
  }
}
