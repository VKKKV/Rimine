package io.github.vkkkv.rimine.jni;

import com.sun.jna.Structure;
import java.util.List;

@Structure.FieldOrder({
  "data_size",
  "schema_id",
  "schema_name",
  "is_disabled",
  "is_composing",
  "is_ascii_mode",
  "is_full_shape",
  "is_simplified",
  "is_traditional",
  "is_ascii_punct"
})
public class RimeStatus extends Structure {
  public int data_size;
  public String schema_id;
  public String schema_name;
  public int is_disabled;
  public int is_composing;
  public int is_ascii_mode;
  public int is_full_shape;
  public int is_simplified;
  public int is_traditional;
  public int is_ascii_punct;

  public RimeStatus() {
    super();
    // Mimic RIME_STRUCT_INIT: data_size = sizeof(Type) - sizeof(data_size)
    int sz = this.size();
    this.data_size = sz - 4; // sizeof(int) = 4
  }

  @Override
  protected List<String> getFieldOrder() {
    return List.of(
        "data_size",
        "schema_id",
        "schema_name",
        "is_disabled",
        "is_composing",
        "is_ascii_mode",
        "is_full_shape",
        "is_simplified",
        "is_traditional",
        "is_ascii_punct");
  }
}
