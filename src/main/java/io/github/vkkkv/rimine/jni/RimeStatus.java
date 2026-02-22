package io.github.vkkkv.rimine.jni;

import com.sun.jna.Structure;
import java.util.Arrays;
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
  public boolean is_disabled;
  public boolean is_composing;
  public boolean is_ascii_mode;
  public boolean is_full_shape;
  public boolean is_simplified;
  public boolean is_traditional;
  public boolean is_ascii_punct;

  public RimeStatus() {
    super();
    this.data_size = this.size();
  }

  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList(
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
