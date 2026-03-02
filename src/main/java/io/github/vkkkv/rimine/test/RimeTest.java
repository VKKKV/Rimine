package io.github.vkkkv.rimine.test;

import com.sun.jna.Pointer;
import io.github.vkkkv.rimine.jni.RimeApi;
import io.github.vkkkv.rimine.jni.RimeCandidate;
import io.github.vkkkv.rimine.jni.RimeContext;
import io.github.vkkkv.rimine.jni.RimeLib;
import io.github.vkkkv.rimine.jni.RimeSchemaList;
import io.github.vkkkv.rimine.jni.RimeStatus;
import io.github.vkkkv.rimine.jni.RimeTraits;

/**
 * Simple test program to verify RIME initialization and basic functionality
 */
public class RimeTest {
  public static void main(String[] args) {
    System.out.println("=== RIME Input Method Test ===\n");
    RimeApi.ByReference api = null;
    long sessionId = 0;

    try {
      // Step 1: Get API
      System.out.println("[1] Loading RIME library...");
      api = RimeLib.INSTANCE.rime_get_api();
      if (api == null) {
        System.err.println("❌ Failed to get RIME API!");
        return;
      }
      System.out.println("✓ RIME API loaded successfully");
      System.out.println("   API data_size: " + api.data_size);

      // Step 2: Initialize
      System.out.println("\n[2] Initializing RIME...");
      RimeTraits traits = new RimeTraits();
      traits.shared_data_dir = "/usr/share/rime-data";
      traits.user_data_dir = System.getProperty("user.home") + "/.local/share/rime";
      traits.app_name = "rimine_test";

      // Create ByReference for C interop if needed
      RimeTraits.ByReference traitsRef = new RimeTraits.ByReference();
      traitsRef.shared_data_dir = traits.shared_data_dir;
      traitsRef.user_data_dir = traits.user_data_dir;
      traitsRef.app_name = traits.app_name;
      traitsRef.data_size = traits.data_size;
      
      api.setup.invoke(traitsRef);
      System.out.println("✓ setup() called");

      api.initialize.invoke(traitsRef);
      System.out.println("✓ initialize() called");

      // Step 3: Create session
      System.out.println("\n[3] Creating RIME session...");
      sessionId = api.create_session.invoke();
      if (sessionId == 0) {
        System.err.println("❌ Failed to create session!");
        return;
      }
      System.out.println("✓ Session created: ID = " + sessionId);

      // Step 4: Get current schema
      System.out.println("\n[4] Getting schema list...");
      RimeSchemaList schemaList = new RimeSchemaList();
      boolean gotList = api.get_schema_list.invoke(schemaList);
      if (gotList) {
        System.out.println("✓ Schema list retrieved: " + schemaList.size + " schemas");
      } else {
        System.out.println("⚠ Could not get schema list");
      }

      // Step 5: Get status
      System.out.println("\n[5] Getting status...");
      RimeStatus status = new RimeStatus();
      boolean gotStatus = api.get_status.invoke(sessionId, status);
      if (gotStatus) {
        System.out.println("✓ Status retrieved:");
        System.out.println("   Schema: " + (status.schema_id != null ? status.schema_id : "N/A"));
        System.out.println("   ASCII mode: " + status.is_ascii_mode);
        System.out.println("   Composing: " + status.is_composing);
        api.free_status.invoke(status);
      } else {
        System.out.println("❌ Failed to get status");
      }

      // Step 6: Test key processing
      System.out.println("\n[6] Testing key processing...");
      System.out.println("   Sending key: 'h' (0x68)");
      boolean handled = api.process_key.invoke(sessionId, 0x68, 0);
      System.out.println("   Key handled: " + handled);

      // Step 7: Get context
      System.out.println("\n[7] Getting context...");
      RimeContext context = new RimeContext();
      boolean gotContext = api.get_context.invoke(sessionId, context);
      if (gotContext) {
        System.out.println("✓ Context retrieved:");
        System.out.println("   Preedit: " + (context.composition.preedit != null ? context.composition.preedit : "(empty)"));
        System.out.println("   Candidates: " + context.menu.num_candidates);
        
        if (context.menu.num_candidates > 0) {
          System.out.println("   ✓ Candidates found!");
          System.out.println("   Page " + context.menu.page_no + (context.menu.is_last_page != 0 ? " (last)" : ""));
          
          // Display candidates
          int structSize = new RimeCandidate(context.menu.candidates).size();
          for (int i = 0; i < Math.min(5, context.menu.num_candidates); i++) {
            Pointer p = context.menu.candidates.share((long) i * structSize);
            RimeCandidate cand = new RimeCandidate(p);
            System.out.println("     [" + i + "] " + cand.text + 
                             (cand.comment != null ? " (" + cand.comment + ")" : ""));
          }
        }
        api.free_context.invoke(context);
      }

      System.out.println("\n=== Test Completed Successfully ===");

    } catch (Exception e) {
      System.err.println("\n❌ Test failed with exception:");
      e.printStackTrace();
    } finally {
      if (api != null) {
        System.out.println("\n[8] Cleanup...");
        if (sessionId != 0) {
          api.destroy_session.invoke(sessionId);
          System.out.println("✓ Session destroyed");
        }
        api.finalize.invoke();
        System.out.println("✓ RIME finalized");
      }
    }
  }
}
