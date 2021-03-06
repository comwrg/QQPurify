package me.zpp0196.qqsimple.hook;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import me.zpp0196.qqsimple.hook.base.BaseHook;
import me.zpp0196.qqsimple.hook.util.HookUtil;

import static me.zpp0196.qqsimple.hook.comm.Classes.ActionSheet;
import static me.zpp0196.qqsimple.hook.comm.Classes.BaseActivity;
import static me.zpp0196.qqsimple.hook.comm.Classes.BusinessInfoCheckUpdate$RedTypeInfo;
import static me.zpp0196.qqsimple.hook.comm.Classes.Contacts;
import static me.zpp0196.qqsimple.hook.comm.Classes.Conversation;
import static me.zpp0196.qqsimple.hook.comm.Classes.ConversationNowController;
import static me.zpp0196.qqsimple.hook.comm.Classes.Leba;
import static me.zpp0196.qqsimple.hook.comm.Classes.LocalSearchBar;
import static me.zpp0196.qqsimple.hook.comm.Classes.MainFragment;
import static me.zpp0196.qqsimple.hook.comm.Classes.PopupMenuDialog;
import static me.zpp0196.qqsimple.hook.comm.Classes.PopupMenuDialog$MenuItem;
import static me.zpp0196.qqsimple.hook.comm.Classes.PopupMenuDialog$OnClickActionListener;
import static me.zpp0196.qqsimple.hook.comm.Classes.SimpleSlidingIndicator;
import static me.zpp0196.qqsimple.hook.comm.Classes.TListView;

/**
 * Created by zpp0196 on 2018/3/12.
 */

class MainUIHook extends BaseHook {

    @Override
    public void init() {
        hideMenuUAF();
        hidePopupMenuEntry();
        hidePopupMenuContacts();
        hideMainFragmentTab();
        hideSearchContainer();
        hideContactsConstant();
        // 隐藏消息界面全民闯关入口
        findAndHookMethod(ConversationNowController, "a", String.class, replaceNull("hide_national_entrance"));
        if (HookUtil.isMoreThan735()) {
            // 隐藏动态界面大家都在搜
            findAndHookMethod(Leba, "a", List.class, replaceObj("hide_everyone_searching"));
        }
    }

    /**
     * 隐藏菜单更新和反馈
     */
    private void hideMenuUAF() {
        findAndHookMethod(MainFragment, "r", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Field field = findField(MainFragment, Dialog.class, "b");
                if (field == null) {
                    return;
                }
                Dialog dialog = (Dialog) field.get(param.thisObject);
                if (dialog == null) {
                    return;
                }
                Field field1 = findFirstFieldByExactType(ActionSheet, LinearLayout.class);
                if (field1 == null) {
                    return;
                }
                if (!getBool("hide_menu_uaf")) {
                    return;
                }
                LinearLayout layout = (LinearLayout) field1.get(dialog);
                layout.getChildAt(0)
                        .setVisibility(View.GONE);
                layout.getChildAt(1)
                        .setVisibility(View.GONE);
            }
        });
    }

    /**
     * 隐藏消息列表右上角快捷入口
     */
    private void hidePopupMenuEntry() {
        findAndHookMethod(Conversation, "D", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                ImageView view = (ImageView) findField(Conversation, ImageView.class, "a").get(param.thisObject);
                if (!getBool("hide_popup_menu_entry")) {
                    return;
                }
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 隐藏消息列表右上角快捷入口内容
     */
    @SuppressWarnings ("unchecked")
    private void hidePopupMenuContacts() {
        findAndHookMethod(PopupMenuDialog, "a", Activity.class, List.class, PopupMenuDialog$OnClickActionListener, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                List list = (List) param.args[1];
                if (list == null || list.isEmpty() || PopupMenuDialog$MenuItem == null) {
                    return;
                }
                List needRemove = new ArrayList();
                for (Object item : list) {
                    String title = (String) findField(PopupMenuDialog$MenuItem, String.class, "a").get(item);
                    if (title.equals("创建群聊") && getBool("hide_popup_multiChat")) {
                        needRemove.add(item);
                    }
                    if (title.equals("加好友/群") && getBool("hide_popup_add")) {
                        needRemove.add(item);
                    }
                    if (title.equals("扫一扫") && getBool("hide_popup_sweep")) {
                        needRemove.add(item);
                    }
                    if (title.equals("面对面快传") && getBool("hide_popup_face2face")) {
                        needRemove.add(item);
                    }
                    if (title.equals("付款") && getBool("hide_popup_pay")) {
                        needRemove.add(item);
                    }
                    if (title.equals("拍摄") && getBool("hide_popup_shoot")) {
                        needRemove.add(item);
                    }
                    if (title.equals("高能舞室") && getBool("hide_popup_videoDance")) {
                        needRemove.add(item);
                    }

                }
                list.removeAll(needRemove);
            }
        });
    }

    /**
     * 隐藏底部分组
     */
    private void hideMainFragmentTab() {
        findAndHookMethod(MainFragment, "a", View.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                View[] views = getObject(param.thisObject, View[].class, "a");
                if (views == null) {
                    return;
                }
                // 联系人
                if (getBool("hide_tab_contact")) {
                    views[2].setVisibility(View.GONE);
                }
                // 动态
                if (getBool("hide_tab_dynamic")) {
                    views[3].setVisibility(View.GONE);
                }
                // 看点
                if (getBool("hide_tab_readinjoy")) {
                    views[6].setVisibility(View.GONE);
                }
            }
        });

        // 隐藏底部消息数量
        findAndHookMethod(MainFragment, "a", int.class, BusinessInfoCheckUpdate$RedTypeInfo, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                int i = (int) param.args[0];
                if ((i == 33 && getBool("hide_tab_contact_num")) ||
                    (i == 34 && (getBool("hide_tab_dynamic_num")))) {
                    param.setResult(null);
                }
            }
        });
    }

    /**
     * 隐藏搜索框
     */
    private void hideSearchContainer() {
        // 消息界面
        findAndHookConstructor(LocalSearchBar, TListView, View.class, View.class, BaseActivity, View.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (!getBool("hide_search_conversation")) {
                    return;
                }
                View view = (View) param.args[4];
                if (view != null) {
                    view.setVisibility(View.GONE);
                    ViewGroup listView = (ViewGroup) param.args[0];
                    Activity activity = (Activity) param.args[3];
                    int paddingTop = (int) (activity.getResources()
                                                    .getDisplayMetrics().density * 43 + 0.5f);
                    listView.setPadding(0, -paddingTop, 0, 0);
                }
            }
        });

        // 联系人界面
        findAndHookMethod(Contacts, "o", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (!getBool("hide_search_contacts")) {
                    return;
                }
                LinearLayout layout = getObject(param.thisObject, LinearLayout.class, "a");
                View view = layout.findViewById(getIdInQQ("contactHeader"))
                        .findViewById(getIdInQQ("search_container"));
                view.setVisibility(View.GONE);
            }
        });
    }

    /**
     * 隐藏联系人分组
     */
    private void hideContactsConstant() {

        findAndHookMethod(SimpleSlidingIndicator, "a", int.class, View.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                int i = (int) param.args[0];
                View v = (View) param.args[1];
                if ((i == 2 && getBool("hide_contacts_tab_device")) ||
                    (i == 3 && getBool("hide_contacts_tab_phone")) ||
                    (i == 4 && getBool("hide_contacts_tab_pub_account"))) {
                    v.setVisibility(View.GONE);
                }
            }
        });

        findAndHookMethod(SimpleSlidingIndicator, "a", int.class, boolean.class, boolean.class, new XC_MethodHook() {

            private MethodHookParam param;
            private int i;
            private int b;

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                this.i = (int) param.args[0];
                this.b = getObject(param.thisObject, int.class, "b");
                this.param = param;
                boolean isHideDevice = getBool("hide_contacts_tab_device");
                boolean isHidePhone = getBool("hide_contacts_tab_phone");
                boolean isHidePubAccount = getBool("hide_contacts_tab_pub_account");
                if (isHideDevice && !isHidePhone && !isHidePubAccount) {
                    f(2);
                }
                if (isHideDevice && isHidePhone && !isHidePubAccount) {
                    f(2, 3);
                }
                if (isHideDevice && !isHidePhone && isHidePubAccount) {
                    param.args[0] = (b == 1 && i == 2) ? 3 : (b == 3 && i == 4) ? 0 : param.args[0];
                }
                if (isHideDevice && isHidePhone && isHidePubAccount) {
                    if (i != 2 && i != 3 && i != 4) {
                        return;
                    }
                    m(1, 0);
                }
                if (!isHideDevice && isHidePhone && !isHidePubAccount) {
                    f(3);
                }
                if (!isHideDevice && isHidePhone && isHidePubAccount) {
                    f(3, 4);
                }
                if (!isHideDevice && !isHidePhone && isHidePubAccount) {
                    f(4);
                }
            }

            private void f(int pos) {
                if (i != pos) {
                    return;
                }
                m(pos - 1, pos != 4 ? pos + 1 : 0);
            }

            private void f(int pos1, int pos2) {
                if (i != pos1 && i != pos2) {
                    return;
                }
                m(pos1 - 1, pos2 != 4 ? pos2 + 1 : 0);
            }

            private void m(int left, int right) {
                param.args[0] = b == left ? right : b == right ? left : param.args[0];
            }

        });
    }
}
