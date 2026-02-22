package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

@Structure.FieldOrder({"list", "map", "index", "key", "path"})
public class RimeConfigIterator extends Structure {
  public Pointer list;
  public Pointer map;
  public int index;
  public String key;
  public String path;

  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList("list", "map", "index", "key", "path");
  }
}
