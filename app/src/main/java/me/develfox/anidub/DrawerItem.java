package me.develfox.anidub;

/**
 * Created by max on 02.11.2014.
 */
public class DrawerItem {
    String itemName;
    int imgResID;
    String count;
    public boolean isVisible = true;
    public boolean checked = !isVisible;

    public DrawerItem(String itemName, int imgResID, String count) {
        super();
        this.itemName = itemName;
        this.imgResID = imgResID;
        this.count = count;
    }


    public String getCount() {
        return count;
    }
    public void setCount(String count) {
        this.count = count;
    }
    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    public int getImgResID() {
        return imgResID;
    }
    public void setImgResID(int imgResID) {
        this.imgResID = imgResID;
    }
}
