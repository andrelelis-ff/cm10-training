package com.coremedia.blueprint.cae.richtext.filter;

import java.util.ArrayList;
import java.util.List;

class SaxElementStack {
  private ArrayList<SaxElementData> stack = new ArrayList<>();


  // --- general stack features -------------------------------------

  public void push(SaxElementData saxElementData) {
    stack.add(saxElementData);
  }

  public SaxElementData pop() {
    return stack.remove(stack.size()-1);
  }

  public SaxElementData top() {
    return stack.get(stack.size()-1);
  }

  public boolean isEmpty() {
    return stack.isEmpty();
  }

  public void clear() {
    stack.clear();
  }


  // --- special features -------------------------------------------

  public int indexOfTag(String tagName) {
    for (int i=stack.size()-1; i>=0; --i) {
      if (stack.get(i).isA(tagName)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns a view of a portion of the stack.
   * <p>
   * The result becomes invalid the next time the stack is modified.
   * </p>
   */
  public List<SaxElementData> subStack(String tagName) {
    int i = indexOfTag(tagName);
    return i>=0 ? stack.subList(i, stack.size()) : null;
  }
}
