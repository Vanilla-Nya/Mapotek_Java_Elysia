package Helpers;

/**
 *
 * @author asuna
 */
public interface OnPasienAddedListener {

    void onPasienAdded(String id, String nik, String name, String age, String gender, String phone, String address, String rfid);
}
