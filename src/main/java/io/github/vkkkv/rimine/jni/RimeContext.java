package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
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
    this.data_size = this.size();
  }

  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList("data_size", "composition", "menu", "commit_text_preview", "select_labels");
  }
}
