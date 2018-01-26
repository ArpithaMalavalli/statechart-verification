package ast;

import java.util.ArrayList;
import java.util.List;

public class DeclarationList extends ArrayList<Declaration>{

  private State state;

  public State getState() {
    return this.state;
  }

  // function to lookup a name already exists in the declaration list

  public Declaration lookup(String name) {
    for(Declaration d : this) {
      if(d.vname.equals(name)) {
        return d;
      }
    }
    return null;
  }

  public void setState(State state) {
    this.state = state;
  }

// function to add a new declaraton in the declaration list
  public boolean add(Declaration dec) {
    for(Declaration d : this) {
      if(d.vname.equals(dec.vname)) {
        return false;
      }
    }
    dec.setDeclarationList(this);
    return super.add(dec);
  }

  public String toString() {
    String s = "\n";
    for(Declaration d : this) {
      s += d.toString() + '\n';
    }
    return s;
  }
}
