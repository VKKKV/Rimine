package io.github.vkkkv.rimine.jni;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public interface RimeLib extends Library {
    RimeLib INSTANCE = Native.load("rime", RimeLib.class);

    @Structure.FieldOrder({
        "shared_data_dir", "user_data_dir", "distribution_name", 
        "distribution_code_name", "distribution_version", "app_name", 
        "modules", "min_log_level"
    })
    class RimeTraits extends Structure {
        public String shared_data_dir;
        public String user_data_dir;
        public String distribution_name;
        public String distribution_code_name;
        public String distribution_version;
        public String app_name;
        public Pointer modules;
        public int min_log_level;
    }

    @Structure.FieldOrder({
        "page_size", "page_no", "is_last_page", "highlighted_candidate_index", 
        "num_candidates", "candidates", "select_keys"
    })
    class RimeMenu extends Structure {
        public int page_size;
        public int page_no;
        public boolean is_last_page;
        public int highlighted_candidate_index;
        public int num_candidates;
        public Pointer candidates; // RimeCandidate*
        public String select_keys;
    }

    @Structure.FieldOrder({"text", "comment", "reserved"})
    class RimeCandidate extends Structure {
        public String text;
        public String comment;
        public String reserved;
        
        public RimeCandidate(Pointer p) {
            super(p);
            read();
        }
    }

    @Structure.FieldOrder({
        "data_size", "composition", "caret_pos", "sel_start", "sel_end", 
        "menu", "commit_text_preview", "reserved"
    })
    class RimeContext extends Structure {
        public int data_size;
        public String composition;
        public int caret_pos;
        public int sel_start;
        public int sel_end;
        public RimeMenu menu;
        public String commit_text_preview;
        public Pointer reserved;

        public RimeContext() {
            super();
            data_size = size();
        }
    }

    void RimeInitialize(RimeTraits traits);
    void RimeFinalize();
    
    long RimeCreateSession();
    boolean RimeDestroySession(long session_id);
    
    boolean RimeProcessKey(long session_id, int keycode, int mask);
    boolean RimeGetContext(long session_id, RimeContext context);
    boolean RimeFreeContext(RimeContext context);
}
