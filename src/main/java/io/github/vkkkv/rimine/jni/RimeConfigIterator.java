package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
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
    return List.of("list", "map", "index", "key", "path");
  }
}
