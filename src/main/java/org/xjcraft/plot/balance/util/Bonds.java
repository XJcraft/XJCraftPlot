package org.xjcraft.plot.balance.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * 国债工具类
 */
public class Bonds {
    /**
     * 获取国债的额度
     * @param itemStack 国债
     * @return 国债的额度，注意，如果是堆叠的国债，如 10 本 4 元，也只会输出 4
     *         如果入参不是国债，则会输出 0
     */
    public static int getValue(ItemStack itemStack) {
        // 滤掉 null
        if (itemStack == null) {
            return 0;
        }

        // 滤掉不是书的
        if (!(itemStack.getType() == Material.WRITTEN_BOOK)) {
            return 0;
        }
        if (!(itemStack.getItemMeta() instanceof BookMeta)) {
            return 0;
        }

        // 书的数据
        var meta = (BookMeta) itemStack.getItemMeta();

        // 滤掉作者不对的
        if (!"G".equals(meta.getAuthor())) {
            return 0;
        }
        // 滤掉不是破旧不堪的
        if (meta.getGeneration() != BookMeta.Generation.TATTERED) {
            return 0;
        }

        // 滤掉多于一页的
        if (meta.getPageCount() != 1) {
            return 0;
        }

        // 书的标题
        var title = meta.getTitle();
        // 书的内容(第一页)
        var content = meta.getPage(1);

        // 先预防下 NPE，虽然应该不会出现 0.0
        if (title == null) {
            return 0;
        }

        // 判断价值
        if (title.equals("4元") && content.equals("国债4元§8§n  XJcraft政府公开发行§7§kABCDEFG")) {
            return 4;
        } else if (title.equals("64元") && content.equals("国债64元§8§n  XJcraft政府公开发行§7§kABCDEFG")) {
            return 64;
        } else if (title.equals("1024元") && content.equals("国债1024元§8§n  XJcraft政府公开发行§7§kABCDEFG")) {
            return 1024;
        } else {
            return 0;
        }
    }
}
