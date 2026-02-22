package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

@Structure.FieldOrder({"text", "comment", "reserved"})
public class RimeCandidate extends Structure {
  public String text;
  public String comment;
  public Pointer reserved;

  public RimeCandidate() {
    super();
  }

  public RimeCandidate(Pointer p) {
    super(p);
    read();
  }

  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList("text", "comment", "reserved");
  }
}
