package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

@Structure.FieldOrder({"ptr", "index", "candidate"})
public class RimeCandidateListIterator extends Structure {
  public Pointer ptr;
  public int index;
  public RimeCandidate candidate;

  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList("ptr", "index", "candidate");
  }
}
