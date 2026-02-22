package io.github.vkkkv.rimine.jni;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

@Structure.FieldOrder({"data_size", "text"})
public class RimeCommit extends Structure {
  public int data_size;
  public String text;

  public RimeCommit() {
    super();
    this.data_size = this.size();
  }

  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList("data_size", "text");
  }
}
