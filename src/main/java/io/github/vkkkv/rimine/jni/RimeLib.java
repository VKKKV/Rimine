package io.github.vkkkv.rimine.jni;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import io.github.vkkkv.rimine.core.LibraryLoader;

public interface RimeLib extends Library {
  RimeLib INSTANCE = LibraryLoader.load("rime", RimeLib.class);

  @Structure.FieldOrder({
    "data_size",
    "shared_data_dir",
    "user_data_dir",
    "distribution_name",
    "distribution_code_name",
    "distribution_version",
    "app_name",
    "modules",
    "min_log_level",
    "log_dir",
    "prebuilt_data_dir",
    "staging_dir"
  })
  class RimeTraits extends Structure {
    public int data_size;
    public String shared_data_dir;
    public String user_data_dir;
    public String distribution_name;
    public String distribution_code_name;
    public String distribution_version;
    public String app_name;
    public Pointer modules;
    public int min_log_level;
    public String log_dir;
    public String prebuilt_data_dir;
    public String staging_dir;

    public RimeTraits() {
      super();
      this.data_size = this.size();
    }
  }

  @Structure.FieldOrder({"length", "cursor_pos", "sel_start", "sel_end", "preedit"})
  class RimeComposition extends Structure {
    public int length;
    public int cursor_pos;
    public int sel_start;
    public int sel_end;
    public String preedit;
  }

  @Structure.FieldOrder({"text", "comment", "reserved"})
  class RimeCandidate extends Structure {
    public String text;
    public String comment;
    public Pointer reserved;

    public RimeCandidate(Pointer p) {
      super(p);
      read();
    }
  }

  @Structure.FieldOrder({
    "page_size",
    "page_no",
    "is_last_page",
    "highlighted_candidate_index",
    "num_candidates",
    "candidates",
    "select_keys"
  })
  class RimeMenu extends Structure {
    public int page_size;
    public int page_no;
    public boolean is_last_page;
    public int highlighted_candidate_index;
    public int num_candidates;
    public Pointer candidates;
    public String select_keys;
  }

  @Structure.FieldOrder({"data_size", "text"})
  class RimeCommit extends Structure {
    public int data_size;
    public String text;

    public RimeCommit() {
      super();
      this.data_size = this.size();
    }
  }

  @Structure.FieldOrder({"data_size", "composition", "menu", "commit_text_preview", "select_labels"})
  class RimeContext extends Structure {
    public int data_size;
    public RimeComposition composition;
    public RimeMenu menu;
    public String commit_text_preview;
    public Pointer select_labels;

    public RimeContext() {
      super();
      this.data_size = this.size();
    }
  }

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
  class RimeStatus extends Structure {
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
  }

  @Structure.FieldOrder({"ptr", "index", "candidate"})
  class RimeCandidateListIterator extends Structure {
    public Pointer ptr;
    public int index;
    public RimeCandidate candidate;
  }

  @Structure.FieldOrder({"ptr"})
  class RimeConfig extends Structure {
    public Pointer ptr;
  }

  @Structure.FieldOrder({"list", "map", "index", "key", "path"})
  class RimeConfigIterator extends Structure {
    public Pointer list;
    public Pointer map;
    public int index;
    public String key;
    public String path;
  }

  @Structure.FieldOrder({"schema_id", "name", "reserved"})
  class RimeSchemaListItem extends Structure {
    public String schema_id;
    public String name;
    public Pointer reserved;
  }

  @Structure.FieldOrder({"size", "list"})
  class RimeSchemaList extends Structure {
    public long size;
    public Pointer list;
  }

  @Structure.FieldOrder({"str", "length"})
  class RimeStringSlice extends Structure {
    public String str;
    public long length;
  }

  // Notification handler callback
  interface RimeNotificationHandler {
    void invoke(Pointer context_object, long session_id, String message_type, String message_value);
  }

  @Structure.FieldOrder({"data_size"})
  class RimeCustomApi extends Structure {
    public int data_size;
  }

  @Structure.FieldOrder({"data_size", "module_name", "initialize", "finalize", "get_api"})
  class RimeModule extends Structure {
    public int data_size;
    public String module_name;
    public Pointer initialize;
    public Pointer finalize;
    public Pointer get_api;
  }

  // Core API functions
  void RimeInitialize(RimeTraits traits);

  void RimeFinalize();

  void RimeSetNotificationHandler(RimeNotificationHandler handler, Pointer context_object);

  boolean RimeStartMaintenance(boolean full_check);

  boolean RimeIsMaintenanceMode();

  void RimeJoinMaintenanceThread();

  // Deployment
  void RimeDeployerInitialize(RimeTraits traits);

  boolean RimePrebuild();

  boolean RimeDeploy();

  boolean RimeDeploySchema(String schema_file);

  boolean RimeDeployConfigFile(String file_name, String version_key);

  boolean RimeSyncUserData();

  // Session management
  long RimeCreateSession();

  boolean RimeFindSession(long session_id);

  boolean RimeDestroySession(long session_id);

  void RimeCleanupStaleSessions();

  void RimeCleanupAllSessions();

  // Input
  boolean RimeProcessKey(long session_id, int keycode, int mask);

  boolean RimeCommitComposition(long session_id);

  void RimeClearComposition(long session_id);

  // Output
  boolean RimeGetCommit(long session_id, RimeCommit commit);

  boolean RimeFreeCommit(RimeCommit commit);

  boolean RimeGetContext(long session_id, RimeContext context);

  boolean RimeFreeContext(RimeContext context);

  boolean RimeGetStatus(long session_id, RimeStatus status);

  boolean RimeFreeStatus(RimeStatus status);

  // Runtime options
  void RimeSetOption(long session_id, String option, boolean value);

  boolean RimeGetOption(long session_id, String option);

  void RimeSetProperty(long session_id, String prop, String value);

  boolean RimeGetProperty(long session_id, String prop, byte[] value, long buffer_size);

  boolean RimeGetSchemaList(RimeSchemaList schema_list);

  void RimeFreeSchemaList(RimeSchemaList schema_list);

  boolean RimeGetCurrentSchema(long session_id, byte[] schema_id, long buffer_size);

  boolean RimeSelectSchema(long session_id, String schema_id);

  // Configuration
  boolean RimeSchemaOpen(String schema_id, RimeConfig config);

  boolean RimeConfigOpen(String config_id, RimeConfig config);

  boolean RimeConfigClose(RimeConfig config);

  boolean RimeConfigGetBool(RimeConfig config, String key, boolean[] value);

  boolean RimeConfigGetInt(RimeConfig config, String key, int[] value);

  boolean RimeConfigGetDouble(RimeConfig config, String key, double[] value);

  boolean RimeConfigGetString(RimeConfig config, String key, byte[] value, long buffer_size);

  String RimeConfigGetCString(RimeConfig config, String key);

  boolean RimeConfigUpdateSignature(RimeConfig config, String signer);

  boolean RimeConfigBeginMap(RimeConfigIterator iterator, RimeConfig config, String key);

  boolean RimeConfigNext(RimeConfigIterator iterator);

  void RimeConfigEnd(RimeConfigIterator iterator);

  // Testing
  boolean RimeSimulateKeySequence(long session_id, String key_sequence);

  // Module
  boolean RimeRegisterModule(RimeModule module);

  RimeModule RimeFindModule(String module_name);

  boolean RimeRunTask(String task_name);

  // Deprecated: use get_shared_data_dir_s instead
  String RimeGetSharedDataDir();

  // Deprecated: use get_user_data_dir_s instead
  String RimeGetUserDataDir();

  // Deprecated: use get_sync_dir_s instead
  String RimeGetSyncDir();

  String RimeGetUserId();

  void RimeGetUserDataSyncDir(byte[] dir, long buffer_size);

  // Configuration management
  boolean RimeConfigInit(RimeConfig config);

  boolean RimeConfigLoadString(RimeConfig config, String yaml);

  boolean RimeConfigSetBool(RimeConfig config, String key, boolean value);

  boolean RimeConfigSetInt(RimeConfig config, String key, int value);

  boolean RimeConfigSetDouble(RimeConfig config, String key, double value);

  boolean RimeConfigSetString(RimeConfig config, String key, String value);

  boolean RimeConfigGetItem(RimeConfig config, String key, RimeConfig value);

  boolean RimeConfigSetItem(RimeConfig config, String key, RimeConfig value);

  boolean RimeConfigClear(RimeConfig config, String key);

  boolean RimeConfigCreateList(RimeConfig config, String key);

  boolean RimeConfigCreateMap(RimeConfig config, String key);

  long RimeConfigListSize(RimeConfig config, String key);

  boolean RimeConfigBeginList(RimeConfigIterator iterator, RimeConfig config, String key);

  // Input/Caret
  String RimeGetInput(long session_id);

  long RimeGetCaretPos(long session_id);

  boolean RimeSelectCandidate(long session_id, long index);

  String RimeGetVersion();

  void RimeSetCaretPos(long session_id, long caret_pos);

  boolean RimeSelectCandidateOnCurrentPage(long session_id, long index);

  // Candidate list access
  boolean RimeCandidateListBegin(long session_id, RimeCandidateListIterator iterator);

  boolean RimeCandidateListNext(RimeCandidateListIterator iterator);

  void RimeCandidateListEnd(RimeCandidateListIterator iterator);

  // User config
  boolean RimeUserConfigOpen(String config_id, RimeConfig config);

  boolean RimeCandidateListFromIndex(long session_id, RimeCandidateListIterator iterator, int index);

  // Deprecated: use get_prebuilt_data_dir_s instead
  String RimeGetPrebuiltDataDir();

  // Deprecated: use get_staging_dir_s instead
  String RimeGetStagingDir();

  // Capsule proto functions (deprecated for capnproto API)
  void RimeCommitProto(long session_id, Pointer commit_builder);

  void RimeContextProto(long session_id, Pointer context_builder);

  void RimeStatusProto(long session_id, Pointer status_builder);

  String RimeGetStateLabel(long session_id, String option_name, boolean state);

  // Candidate deletion
  boolean RimeDeleteCandidate(long session_id, long index);

  boolean RimeDeleteCandidateOnCurrentPage(long session_id, long index);

  RimeStringSlice RimeGetStateLabelAbbreviated(long session_id, String option_name, boolean state,
      boolean abbreviated);

  boolean RimeSetInput(long session_id, String input);

  // Directory functions (string buffer versions)
  void RimeGetSharedDataDirS(byte[] dir, long buffer_size);

  void RimeGetUserDataDirS(byte[] dir, long buffer_size);

  void RimeGetPrebuiltDataDirS(byte[] dir, long buffer_size);

  void RimeGetStagingDirS(byte[] dir, long buffer_size);

  void RimeGetSyncDirS(byte[] dir, long buffer_size);

  // Highlighting
  boolean RimeHighlightCandidate(long session_id, long index);

  boolean RimeHighlightCandidateOnCurrentPage(long session_id, long index);

  // Pagination
  boolean RimeChangePage(long session_id, boolean backward);
}
