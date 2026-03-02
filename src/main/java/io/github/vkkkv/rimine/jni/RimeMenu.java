package io.github.vkkkv.rimine.jni;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.List;

@Structure.FieldOrder({
  "page_size",
  "page_no",
  "is_last_page",
  "highlighted_candidate_index",
  "num_candidates",
  "candidates",
  "select_keys"
})
public class RimeMenu extends Structure {
  public int page_size;
  public int page_no;
  public int is_last_page;
  public int highlighted_candidate_index;
  public int num_candidates;
  public Pointer candidates;
  public String select_keys;

  @Override
  protected List<String> getFieldOrder() {
    return List.of(
        "page_size",
        "page_no",
        "is_last_page",
        "highlighted_candidate_index",
        "num_candidates",
        "candidates",
        "select_keys");
  }
}
