package drm.bot.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.PermissionUtil;

public class AdminChecker {

    // Define the condition for admin privileges
    public static boolean isAdmin(Member member) {
        // Check if the user is a member of the server
        if (member != null) {
            // Use PermissionUtil to check if the user has the ADMINISTRATOR permission
            return PermissionUtil.checkPermission(member, Permission.ADMINISTRATOR); // User has admin privileges
        }
        return false; // User does not have admin privileges
    }
}