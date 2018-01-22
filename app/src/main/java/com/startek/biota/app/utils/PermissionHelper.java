package com.startek.biota.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skt90u on 2016/5/24.
 */
public class PermissionHelper {

    private Activity context;

    public PermissionHelper(Activity context)
    {
        this.context = context;
    }

    public boolean checkRequestedPermissions()
    {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List pkgAppsList = context.getPackageManager().queryIntentActivities(mainIntent, 0);

        String[] requestedPermissions = new String[]{};

        for (Object obj : pkgAppsList)
        {
            ResolveInfo resolveInfo = (ResolveInfo) obj;
            PackageInfo packageInfo = null;
            try {
                packageInfo = context.getPackageManager().getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_PERMISSIONS);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(packageInfo != null)
            {
                requestedPermissions = packageInfo.requestedPermissions;
            }
        }

        List<String> errors = new ArrayList<String>();

        for(String permission:requestedPermissions)
        {
            if(PackageManager.PERMISSION_GRANTED != context.checkCallingOrSelfPermission(permission))
            {
                String error = String.format("無權限 - %s", permission);
                errors.add(error);
            }
        }

        if(errors.size() != 0)
        {
            String errmsg = "";
            for(int i=0; i<errors.size(); i++)
            {
                errmsg += String.format("%d. %s\n", i+1, errors.get(i));
            }

            DialogHelper.alert(context, errmsg);
            return false;
        }

        return true;
    }

    public boolean hasPermission(String permission)
    {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (info.requestedPermissions != null) {
                for (String p : info.requestedPermissions) {
                    if (p.equals(permission)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
