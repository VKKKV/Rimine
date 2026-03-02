package io.github.vkkkv.rimine.test;

import com.sun.jna.Pointer;
import io.github.vkkkv.rimine.jni.RimeApi;
import io.github.vkkkv.rimine.jni.RimeCandidate;
import io.github.vkkkv.rimine.jni.RimeContext;
import io.github.vkkkv.rimine.jni.RimeLib;
import io.github.vkkkv.rimine.jni.RimeStatus;
import io.github.vkkkv.rimine.jni.RimeTraits;

/**
 * Test RIME with proper input schema for candidate generation
 */
public class RimeTestWithSchema {
  public static void main(String[] args) {
    System.out.println("=== RIME Input Method Test (With Schema) ===\n");
    RimeApi.ByReference api = null;
    long sessionId = 0;

    try {
      api = RimeLib.INSTANCE.rime_get_api();
      if (api == null) {
        System.err.println("❌ Failed to get RIME API!");
        return;
      }
      System.out.println("✓ RIME API loaded");

      // Initialize RIME
      RimeTraits traits = new RimeTraits();
      traits.shared_data_dir = "/usr/share/rime-data";
      traits.user_data_dir = System.getProperty("user.home") + "/.local/share/rime";
      traits.app_name = "rimine_test_schema";
      
      RimeTraits.ByReference traitsRef = new RimeTraits.ByReference();
      traitsRef.shared_data_dir = traits.shared_data_dir;
      traitsRef.user_data_dir = traits.user_data_dir;
      traitsRef.app_name = traits.app_name;
      traitsRef.data_size = traits.data_size;

      api.setup.invoke(traitsRef);
      api.initialize.invoke(traitsRef);
      System.out.println("✓ RIME initialized");

      // Create session
      sessionId = api.create_session.invoke();
      System.out.println("✓ Session created: ID = " + sessionId);

      // Get and print current schema
      RimeStatus status = new RimeStatus();
      if (api.get_status.invoke(sessionId, status)) {
        System.out.println("\n📝 Current schema: " + status.schema_id);
        
        // Try to select luna_pinyin if available
        System.out.println("\n🔄 Attempting to select 'luna_pinyin' schema...");
        if (api.select_schema.invoke(sessionId, "luna_pinyin")) {
          System.out.println("✓ Schema switched to 'luna_pinyin'");
          
          // Verify new schema
          RimeStatus status2 = new RimeStatus();
          if (api.get_status.invoke(sessionId, status2)) {
            System.out.println("   Current: " + status2.schema_id);
          }
        } else {
          System.out.println("⚠ Could not switch schema (may not be installed)");
        }
        
        api.free_status.invoke(status);
      }

      // Process some keys to generate candidates
      System.out.println("\n⌨️  Processing key sequence: 'n', 'i', 'h'");
      int[] keys = {0x6e, 0x69, 0x68}; // n, i, h
      
      for (int key : keys) {
        System.out.print("  [" + (char)key + "] ");
        boolean handled = api.process_key.invoke(sessionId, key, 0);
        System.out.print("handled=" + handled);
        
        RimeContext ctx = new RimeContext();
        if (api.get_context.invoke(sessionId, ctx)) {
          if (ctx.composition.preedit != null && !ctx.composition.preedit.isEmpty()) {
            System.out.print(", preedit='" + ctx.composition.preedit + "'");
          }
          if (ctx.menu.num_candidates > 0) {
            System.out.print(", candidates=" + ctx.menu.num_candidates);
          }
          api.free_context.invoke(ctx);
        }
        System.out.println();
      }

      // Get final context
      System.out.println("\n📊 Final Context:");
      RimeContext context = new RimeContext();
      if (api.get_context.invoke(sessionId, context)) {
        System.out.println("   Preedit: " + (context.composition.preedit != null ? context.composition.preedit : "(empty)"));
        System.out.println("   Candidates: " + context.menu.num_candidates);
        
        if (context.menu.num_candidates > 0) {
          System.out.println("   Highlighted: " + context.menu.highlighted_candidate_index);
          System.out.println("\n   Candidate List:");
          
          int structSize = new RimeCandidate(context.menu.candidates).size();
          for (int i = 0; i < Math.min(10, context.menu.num_candidates); i++) {
            Pointer p = context.menu.candidates.share((long) i * structSize);
            RimeCandidate cand = new RimeCandidate(p);
            String marker = (i == context.menu.highlighted_candidate_index) ? " ➜ " : "   ";
            System.out.println(marker + "[" + i + "] " + cand.text + 
                             (cand.comment != null ? " (" + cand.comment + ")" : ""));
          }
        }
        api.free_context.invoke(context);
      }

      System.out.println("\n✓ Test completed");
    } catch (Exception e) {
      System.err.println("\n❌ Test failed:");
      e.printStackTrace();
    } finally {
      if (api != null) {
        if (sessionId != 0) {
          api.destroy_session.invoke(sessionId);
        }
        api.finalize.invoke();
      }
    }
  }
}
