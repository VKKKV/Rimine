#include <jni.h>
#include <rime_api.h>
#include <iostream>

// JNI 必须以纯 C 的 ABI 导出函数，否则 JVM 的 dlsym 一样会报 UnsatisfiedLinkError
extern "C" {

// 注意这里的函数名：Java_包名_类名_方法名 (下划线分隔)
JNIEXPORT void JNICALL
Java_com_kita_rimine_RimeBridge_initEngine(JNIEnv *env, jclass clazz, jstring shared_data, jstring user_data) {

    // 1. 获取 Rime 的 API 实例 (你刚才确认过的干净入口)
    RimeApi* rime = rime_get_api();
    if (!rime) {
        std::cerr << "[Rimine JNI] Fatal Error: Failed to get Rime API struct!" << std::endl;
        return;
    }

    // 2. 将臃肿的 Java String 转换为干净的 C 风格字符串 (指针)
    const char *shared_dir_c = env->GetStringUTFChars(shared_data, nullptr);
    const char *user_dir_c = env->GetStringUTFChars(user_data, nullptr);

    // 3. 构造 RimeTraits
    // 在 C++ 里操作 Struct 简直是享受，不用像 JNA 那样算字节偏移量
    RimeTraits traits;
    // 使用宏初始化，自动填充 data_size，这是上游保证 ABI 兼容性的做法
    RIME_STRUCT_INIT(RimeTraits, traits);

    traits.shared_data_dir = shared_dir_c;
    traits.user_data_dir = user_dir_c;
    traits.distribution_name = "Rimine";
    traits.distribution_code_name = "rimine";
    traits.distribution_version = "1.0.0";
    traits.app_name = "rimine"; // 给你的 Mod 设个专属 name

    // 4. 通过函数指针调用初始化流程
    std::cout << "[Rimine JNI] Setting up Rime engine..." << std::endl;
    rime->setup(&traits);
    rime->initialize(&traits);

    // 5. 释放内存，保持内存整洁，拒绝 Windows 式的内存泄漏
    env->ReleaseStringUTFChars(shared_data, shared_dir_c);
    env->ReleaseStringUTFChars(user_data, user_dir_c);

    std::cout << "[Rimine JNI] Rime engine initialized successfully!" << std::endl;
}

} // extern "C"
