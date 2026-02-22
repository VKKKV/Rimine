package io.github.vkkkv.rimine.jni;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class RimeApi extends Structure {
  public static class ByReference extends RimeApi implements Structure.ByReference {}

  public int data_size;

  // Setup and initialization
  public interface Setup extends Callback {
    void invoke(RimeTraits.ByReference traits);
  }

  public Setup setup;

  public interface SetNotificationHandler extends Callback {
    void invoke(RimeLib.RimeNotificationHandler handler, Pointer context_object);
  }

  public SetNotificationHandler set_notification_handler;

  public interface Initialize extends Callback {
    void invoke(RimeTraits traits);
  }

  public Initialize initialize;

  public interface Finalize extends Callback {
    void invoke();
  }

  public Finalize finalize;

  // Maintenance
  public interface StartMaintenance extends Callback {
    boolean invoke(boolean full_check);
  }

  public StartMaintenance start_maintenance;

  public interface IsMaintenanceMode extends Callback {
    boolean invoke();
  }

  public IsMaintenanceMode is_maintenance_mode;

  public interface JoinMaintenanceThread extends Callback {
    void invoke();
  }

  public JoinMaintenanceThread join_maintenance_thread;

  // Deployment
  public interface DeployerInitialize extends Callback {
    void invoke(RimeTraits traits);
  }

  public DeployerInitialize deployer_initialize;

  public interface Prebuild extends Callback {
    boolean invoke();
  }

  public Prebuild prebuild;

  public interface Deploy extends Callback {
    boolean invoke();
  }

  public Deploy deploy;

  public interface DeploySchema extends Callback {
    boolean invoke(String schema_file);
  }

  public DeploySchema deploy_schema;

  public interface DeployConfigFile extends Callback {
    boolean invoke(String file_name, String version_key);
  }

  public DeployConfigFile deploy_config_file;

  public interface SyncUserData extends Callback {
    boolean invoke();
  }

  public SyncUserData sync_user_data;

  // Session management
  public interface CreateSession extends Callback {
    long invoke();
  }

  public CreateSession create_session;

  public interface FindSession extends Callback {
    boolean invoke(long session_id);
  }

  public FindSession find_session;

  public interface DestroySession extends Callback {
    boolean invoke(long session_id);
  }

  public DestroySession destroy_session;

  public interface CleanupStaleSessions extends Callback {
    void invoke();
  }

  public CleanupStaleSessions cleanup_stale_sessions;

  public interface CleanupAllSessions extends Callback {
    void invoke();
  }

  public CleanupAllSessions cleanup_all_sessions;

  // Input
  public interface ProcessKey extends Callback {
    boolean invoke(long session_id, int keycode, int mask);
  }

  public ProcessKey process_key;

  public interface CommitComposition extends Callback {
    boolean invoke(long session_id);
  }

  public CommitComposition commit_composition;

  public interface ClearComposition extends Callback {
    void invoke(long session_id);
  }

  public ClearComposition clear_composition;

  // Output
  public interface GetCommit extends Callback {
    boolean invoke(long session_id, RimeLib.RimeCommit commit);
  }

  public GetCommit get_commit;

  public interface FreeCommit extends Callback {
    boolean invoke(RimeLib.RimeCommit commit);
  }

  public FreeCommit free_commit;

  public interface GetContext extends Callback {
    boolean invoke(long session_id, RimeLib.RimeContext context);
  }

  public GetContext get_context;

  public interface FreeContext extends Callback {
    boolean invoke(RimeLib.RimeContext context);
  }

  public FreeContext free_context;

  public interface GetStatus extends Callback {
    boolean invoke(long session_id, RimeLib.RimeStatus status);
  }

  public GetStatus get_status;

  public interface FreeStatus extends Callback {
    boolean invoke(RimeLib.RimeStatus status);
  }

  public FreeStatus free_status;

  // Runtime options
  public interface SetOption extends Callback {
    void invoke(long session_id, String option, boolean value);
  }

  public SetOption set_option;

  public interface GetOption extends Callback {
    boolean invoke(long session_id, String option);
  }

  public GetOption get_option;

  public interface SetProperty extends Callback {
    void invoke(long session_id, String prop, String value);
  }

  public SetProperty set_property;

  public interface GetProperty extends Callback {
    boolean invoke(long session_id, String prop, byte[] value, long buffer_size);
  }

  public GetProperty get_property;

  public interface GetSchemaList extends Callback {
    boolean invoke(RimeLib.RimeSchemaList schema_list);
  }

  public GetSchemaList get_schema_list;

  public interface FreeSchemaList extends Callback {
    void invoke(RimeLib.RimeSchemaList schema_list);
  }

  public FreeSchemaList free_schema_list;

  public interface GetCurrentSchema extends Callback {
    boolean invoke(long session_id, byte[] schema_id, long buffer_size);
  }

  public GetCurrentSchema get_current_schema;

  public interface SelectSchema extends Callback {
    boolean invoke(long session_id, String schema_id);
  }

  public SelectSchema select_schema;

  // Configuration
  public interface SchemaOpen extends Callback {
    boolean invoke(String schema_id, RimeLib.RimeConfig config);
  }

  public SchemaOpen schema_open;

  public interface ConfigOpen extends Callback {
    boolean invoke(String config_id, RimeLib.RimeConfig config);
  }

  public ConfigOpen config_open;

  public interface ConfigClose extends Callback {
    boolean invoke(RimeLib.RimeConfig config);
  }

  public ConfigClose config_close;

  public interface ConfigGetBool extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key, boolean[] value);
  }

  public ConfigGetBool config_get_bool;

  public interface ConfigGetInt extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key, int[] value);
  }

  public ConfigGetInt config_get_int;

  public interface ConfigGetDouble extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key, double[] value);
  }

  public ConfigGetDouble config_get_double;

  public interface ConfigGetString extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key, byte[] value, long buffer_size);
  }

  public ConfigGetString config_get_string;

  public interface ConfigGetCString extends Callback {
    String invoke(RimeLib.RimeConfig config, String key);
  }

  public ConfigGetCString config_get_cstring;

  public interface ConfigUpdateSignature extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String signer);
  }

  public ConfigUpdateSignature config_update_signature;

  public interface ConfigBeginMap extends Callback {
    boolean invoke(RimeLib.RimeConfigIterator iterator, RimeLib.RimeConfig config, String key);
  }

  public ConfigBeginMap config_begin_map;

  public interface ConfigNext extends Callback {
    boolean invoke(RimeLib.RimeConfigIterator iterator);
  }

  public ConfigNext config_next;

  public interface ConfigEnd extends Callback {
    void invoke(RimeLib.RimeConfigIterator iterator);
  }

  public ConfigEnd config_end;

  // Testing
  public interface SimulateKeySequence extends Callback {
    boolean invoke(long session_id, String key_sequence);
  }

  public SimulateKeySequence simulate_key_sequence;

  // Module
  public interface RegisterModule extends Callback {
    boolean invoke(RimeLib.RimeModule module);
  }

  public RegisterModule register_module;

  public interface FindModule extends Callback {
    RimeLib.RimeModule invoke(String module_name);
  }

  public FindModule find_module;

  public interface RunTask extends Callback {
    boolean invoke(String task_name);
  }

  public RunTask run_task;

  // Directory functions (deprecated)
  public interface GetSharedDataDir extends Callback {
    String invoke();
  }

  public GetSharedDataDir get_shared_data_dir;

  public interface GetUserDataDir extends Callback {
    String invoke();
  }

  public GetUserDataDir get_user_data_dir;

  public interface GetSyncDir extends Callback {
    String invoke();
  }

  public GetSyncDir get_sync_dir;

  public interface GetUserId extends Callback {
    String invoke();
  }

  public GetUserId get_user_id;

  public interface GetUserDataSyncDir extends Callback {
    void invoke(byte[] dir, long buffer_size);
  }

  public GetUserDataSyncDir get_user_data_sync_dir;

  // Configuration: initialization
  public interface ConfigInit extends Callback {
    boolean invoke(RimeLib.RimeConfig config);
  }

  public ConfigInit config_init;

  public interface ConfigLoadString extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String yaml);
  }

  public ConfigLoadString config_load_string;

  // Configuration: setters
  public interface ConfigSetBool extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key, boolean value);
  }

  public ConfigSetBool config_set_bool;

  public interface ConfigSetInt extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key, int value);
  }

  public ConfigSetInt config_set_int;

  public interface ConfigSetDouble extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key, double value);
  }

  public ConfigSetDouble config_set_double;

  public interface ConfigSetString extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key, String value);
  }

  public ConfigSetString config_set_string;

  // Configuration: complex structures
  public interface ConfigGetItem extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key, RimeLib.RimeConfig value);
  }

  public ConfigGetItem config_get_item;

  public interface ConfigSetItem extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key, RimeLib.RimeConfig value);
  }

  public ConfigSetItem config_set_item;

  public interface ConfigClear extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key);
  }

  public ConfigClear config_clear;

  public interface ConfigCreateList extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key);
  }

  public ConfigCreateList config_create_list;

  public interface ConfigCreateMap extends Callback {
    boolean invoke(RimeLib.RimeConfig config, String key);
  }

  public ConfigCreateMap config_create_map;

  public interface ConfigListSize extends Callback {
    long invoke(RimeLib.RimeConfig config, String key);
  }

  public ConfigListSize config_list_size;

  public interface ConfigBeginList extends Callback {
    boolean invoke(RimeLib.RimeConfigIterator iterator, RimeLib.RimeConfig config, String key);
  }

  public ConfigBeginList config_begin_list;

  // Input access
  public interface GetInput extends Callback {
    String invoke(long session_id);
  }

  public GetInput get_input;

  public interface GetCaretPos extends Callback {
    long invoke(long session_id);
  }

  public GetCaretPos get_caret_pos;

  public interface SelectCandidate extends Callback {
    boolean invoke(long session_id, long index);
  }

  public SelectCandidate select_candidate;

  public interface GetVersion extends Callback {
    String invoke();
  }

  public GetVersion get_version;

  public interface SetCaretPos extends Callback {
    void invoke(long session_id, long caret_pos);
  }

  public SetCaretPos set_caret_pos;

  public interface SelectCandidateOnCurrentPage extends Callback {
    boolean invoke(long session_id, long index);
  }

  public SelectCandidateOnCurrentPage select_candidate_on_current_page;

  // Candidate list access
  public interface CandidateListBegin extends Callback {
    boolean invoke(long session_id, RimeLib.RimeCandidateListIterator iterator);
  }

  public CandidateListBegin candidate_list_begin;

  public interface CandidateListNext extends Callback {
    boolean invoke(RimeLib.RimeCandidateListIterator iterator);
  }

  public CandidateListNext candidate_list_next;

  public interface CandidateListEnd extends Callback {
    void invoke(RimeLib.RimeCandidateListIterator iterator);
  }

  public CandidateListEnd candidate_list_end;

  // User config
  public interface UserConfigOpen extends Callback {
    boolean invoke(String config_id, RimeLib.RimeConfig config);
  }

  public UserConfigOpen user_config_open;

  public interface CandidateListFromIndex extends Callback {
    boolean invoke(long session_id, RimeLib.RimeCandidateListIterator iterator, int index);
  }

  public CandidateListFromIndex candidate_list_from_index;

  // Directory functions (deprecated)
  public interface GetPrebuiltDataDir extends Callback {
    String invoke();
  }

  public GetPrebuiltDataDir get_prebuilt_data_dir;

  public interface GetStagingDir extends Callback {
    String invoke();
  }

  public GetStagingDir get_staging_dir;

  // Proto functions (deprecated)
  public interface CommitProto extends Callback {
    void invoke(long session_id, Pointer commit_builder);
  }

  public CommitProto commit_proto;

  public interface ContextProto extends Callback {
    void invoke(long session_id, Pointer context_builder);
  }

  public ContextProto context_proto;

  public interface StatusProto extends Callback {
    void invoke(long session_id, Pointer status_builder);
  }

  public StatusProto status_proto;

  public interface GetStateLabel extends Callback {
    String invoke(long session_id, String option_name, boolean state);
  }

  public GetStateLabel get_state_label;

  // Candidate deletion
  public interface DeleteCandidate extends Callback {
    boolean invoke(long session_id, long index);
  }

  public DeleteCandidate delete_candidate;

  public interface DeleteCandidateOnCurrentPage extends Callback {
    boolean invoke(long session_id, long index);
  }

  public DeleteCandidateOnCurrentPage delete_candidate_on_current_page;

  public interface GetStateLabelAbbreviated extends Callback {
    RimeLib.RimeStringSlice invoke(
        long session_id, String option_name, boolean state, boolean abbreviated);
  }

  public GetStateLabelAbbreviated get_state_label_abbreviated;

  public interface SetInput extends Callback {
    boolean invoke(long session_id, String input);
  }

  public SetInput set_input;

  // Directory functions (string buffer versions)
  public interface GetSharedDataDirS extends Callback {
    void invoke(byte[] dir, long buffer_size);
  }

  public GetSharedDataDirS get_shared_data_dir_s;

  public interface GetUserDataDirS extends Callback {
    void invoke(byte[] dir, long buffer_size);
  }

  public GetUserDataDirS get_user_data_dir_s;

  public interface GetPrebuiltDataDirS extends Callback {
    void invoke(byte[] dir, long buffer_size);
  }

  public GetPrebuiltDataDirS get_prebuilt_data_dir_s;

  public interface GetStagingDirS extends Callback {
    void invoke(byte[] dir, long buffer_size);
  }

  public GetStagingDirS get_staging_dir_s;

  public interface GetSyncDirS extends Callback {
    void invoke(byte[] dir, long buffer_size);
  }

  public GetSyncDirS get_sync_dir_s;

  // Candidate highlighting
  public interface HighlightCandidate extends Callback {
    boolean invoke(long session_id, long index);
  }

  public HighlightCandidate highlight_candidate;

  public interface HighlightCandidateOnCurrentPage extends Callback {
    boolean invoke(long session_id, long index);
  }

  public HighlightCandidateOnCurrentPage highlight_candidate_on_current_page;

  // Pagination
  public interface ChangePage extends Callback {
    boolean invoke(long session_id, boolean backward);
  }

  public ChangePage change_page;

  @Override
  protected List<String> getFieldOrder() {
    return Arrays.asList(
        "data_size",
        "setup",
        "set_notification_handler",
        "initialize",
        "finalize",
        "start_maintenance",
        "is_maintenance_mode",
        "join_maintenance_thread",
        "deployer_initialize",
        "prebuild",
        "deploy",
        "deploy_schema",
        "deploy_config_file",
        "sync_user_data",
        "create_session",
        "find_session",
        "destroy_session",
        "cleanup_stale_sessions",
        "cleanup_all_sessions",
        "process_key",
        "commit_composition",
        "clear_composition",
        "get_commit",
        "free_commit",
        "get_context",
        "free_context",
        "get_status",
        "free_status",
        "set_option",
        "get_option",
        "set_property",
        "get_property",
        "get_schema_list",
        "free_schema_list",
        "get_current_schema",
        "select_schema",
        "schema_open",
        "config_open",
        "config_close",
        "config_get_bool",
        "config_get_int",
        "config_get_double",
        "config_get_string",
        "config_get_cstring",
        "config_update_signature",
        "config_begin_map",
        "config_next",
        "config_end",
        "simulate_key_sequence",
        "register_module",
        "find_module",
        "run_task",
        "get_shared_data_dir",
        "get_user_data_dir",
        "get_sync_dir",
        "get_user_id",
        "get_user_data_sync_dir",
        "config_init",
        "config_load_string",
        "config_set_bool",
        "config_set_int",
        "config_set_double",
        "config_set_string",
        "config_get_item",
        "config_set_item",
        "config_clear",
        "config_create_list",
        "config_create_map",
        "config_list_size",
        "config_begin_list",
        "get_input",
        "get_caret_pos",
        "select_candidate",
        "get_version",
        "set_caret_pos",
        "select_candidate_on_current_page",
        "candidate_list_begin",
        "candidate_list_next",
        "candidate_list_end",
        "user_config_open",
        "candidate_list_from_index",
        "get_prebuilt_data_dir",
        "get_staging_dir",
        "commit_proto",
        "context_proto",
        "status_proto",
        "get_state_label",
        "delete_candidate",
        "delete_candidate_on_current_page",
        "get_state_label_abbreviated",
        "set_input",
        "get_shared_data_dir_s",
        "get_user_data_dir_s",
        "get_prebuilt_data_dir_s",
        "get_staging_dir_s",
        "get_sync_dir_s",
        "highlight_candidate",
        "highlight_candidate_on_current_page",
        "change_page");
  }
}
