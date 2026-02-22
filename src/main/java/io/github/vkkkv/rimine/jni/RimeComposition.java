package io.github.vkkkv.rimine.jni;

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

@Structure.FieldOrder({"length", "cursor_pos", "sel_start", "sel_end", "preedit"})
public class RimeComposition extends Structure {
  public int length;
  public int cursor_pos;
  public int sel_start;
  public int sel_end;
  public String preedit;

  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList("length", "cursor_pos", "sel_start", "sel_end", "preedit");
  }
}
