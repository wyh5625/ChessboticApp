//
// Created by william on 21-Nov-19.
//

#include<stdio.h>
#include<jni.h>
#include "chess-jni.h"
#include "game_control.h"
#include "user_interface.h"

static GameController* mGameController = NULL;


extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_startble_console_JNI_printLogo(JNIEnv *env, jobject thiz) {
    return (env)->NewStringUTF(printLogo().c_str());
}extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_startble_console_JNI_printMenu(JNIEnv *env, jobject thiz) {
    return (env)->NewStringUTF(printMenu().c_str());
}extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_startble_console_JNI_printSituation(JNIEnv *env, jobject thiz) {
    return (env)->NewStringUTF(printSituation(*mGameController->current_game).c_str());
}extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_startble_console_JNI_printBoard(JNIEnv *env, jobject thiz) {
    return (env)->NewStringUTF(printBoard(*mGameController->current_game).c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_startble_console_JNI_newGame(JNIEnv *env, jobject thiz) {
    mGameController->newGame();
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_startble_console_JNI_saveGame(JNIEnv *env, jobject thiz, jstring file_name) {
    // TODO: implement saveGame()
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_startble_console_JNI_loadGame(JNIEnv *env, jobject thiz, jstring file_name) {
    // TODO: implement loadGame()
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_startble_console_JNI_undoMove(JNIEnv *env, jobject thiz) {
    // TODO: implement undoMove()
}extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_startble_console_JNI_movePiece(JNIEnv *env, jobject thiz, jstring from,
                                                jstring to) {
    const char *nativeStringF = env->GetStringUTFChars(from, 0);
    const char *nativeStringT = env->GetStringUTFChars(to, 0);
    bool status = mGameController->movePiece(nativeStringF, nativeStringT);
    env->ReleaseStringUTFChars(from, nativeStringF);
    env->ReleaseStringUTFChars(to, nativeStringT);
    return status;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_startble_console_JNI_initController(JNIEnv *env, jobject thiz) {
    // TODO: implement initController()
    mGameController = new GameController();
}