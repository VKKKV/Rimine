package io.github.vkkkv.rimine.jni;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

public interface RimeNotificationHandler extends Callback {
  void invoke(Pointer context_object, long session_id, String message_type, String message_value);
}
