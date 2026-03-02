package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.List;

@Structure.FieldOrder({"data_size", "composition", "menu", "commit_text_preview", "select_labels"})
public class RimeContext extends Structure {
  public int data_size;
  public RimeComposition composition;
  public RimeMenu menu;
  public String commit_text_preview;
  public Pointer select_labels;

  public RimeContext() {
    super();
    // Mimic RIME_STRUCT_INIT: data_size = sizeof(Type) - sizeof(data_size)
    int sz = this.size();
    this.data_size = sz - 4; // sizeof(int) = 4
  }

  @Override
  protected List<String> getFieldOrder() {
    return List.of("data_size", "composition", "menu", "commit_text_preview", "select_labels");
  }
}
