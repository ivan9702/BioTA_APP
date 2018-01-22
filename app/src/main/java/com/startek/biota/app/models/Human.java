package com.startek.biota.app.models;

import android.text.TextUtils;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.startek.biota.app.utils.Cloner;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.managers.FileManager;
import com.startek.biota.app.utils.IterableUtils;
import com.startek.biota.app.utils.StrUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Human {

    @DatabaseField(id = true)
    public String id; // 20160404 Norman，Light 說 雖然目前傳遞的是字串形別的整數，但是未來可能傳遞非整數

    // 姓名
    @DatabaseField
    public String name;
    // 工號
    @DatabaseField
    public String bind_id;
    // 職稱
    @DatabaseField
    public String job;
    // 生日
    @DatabaseField
    public String birthday;
    // 性別
    @DatabaseField
    public String gender;
    // 血型
    @DatabaseField
    public String bloodtype;

    // 部門 (使用手機新增的資料，此欄位為空)
    @DatabaseField
    public String dept;
    // 身份 (使用手機新增的資料，此欄位只有第一位新增者為 installer，其他皆為空)
    @DatabaseField
    public String is_manager;

    // 判斷是否為手機資料
    public boolean is_local;

    @DatabaseField(dataType = DataType.DATE_LONG) // 剛剛詢問了一下 當初定議會由server給order by 請確認有留下這個欄位(CreateDate) 這是第一順位 第二順位是使用姓名排序
    public Date createdAt;

    @DatabaseField(dataType = DataType.DATE_LONG) // 剛剛詢問了一下 當初定議會由server給order by 請確認有留下這個欄位(CreateDate) 這是第一順位 第二順位是使用姓名排序
    public Date updatedAt;

    // team status (使用手機新增的資料，此欄位為空)

    // private List<Team> team;

    // private List<String> nfc;

    @Override
    public boolean equals(Object other)
    {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Human)) return false;

        Human lhs = this;
        Human rhs = (Human)other;

        // TextUtils.isEmpty(lhs.name) && TextUtils.isEmpty(rhs.name)

        return StrUtils.equals(lhs.name, rhs.name)
            && StrUtils.equals(lhs.bind_id, rhs.bind_id)
            && StrUtils.equals(lhs.job, rhs.job)
            && StrUtils.equals(lhs.birthday, rhs.birthday)
            && StrUtils.equals(lhs.gender, rhs.gender)
            && StrUtils.equals(lhs.bloodtype, rhs.bloodtype)
            && IterableUtils.equals(lhs.getOriginalNfcs(), rhs.getOriginalNfcs())
            && IterableUtils.equals(lhs.getNfcs(), rhs.getNfcs())
            && IterableUtils.equals(lhs.getOriginalFingerprints(), rhs.getOriginalFingerprints())
            && IterableUtils.equals(lhs.getFingerprints(), rhs.getFingerprints())
                ;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Human{");
        sb.append(",id='").append(id).append('\'');
        sb.append(",name='").append(name).append('\'');
        sb.append(",bind_id='").append(bind_id).append('\'');
        sb.append(",job='").append(job).append('\'');
        sb.append(",birthday='").append(birthday).append('\'');
        sb.append(",gender='").append(gender).append('\'');
        sb.append(",bloodtype='").append(bloodtype).append('\'');
        sb.append(",dept='").append(dept).append('\'');
        sb.append(",is_manager='").append(is_manager).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static final String NEW_DATA_ID = "NEW_DATA_ID";
    public static final String INSTALLER = "installer";

    private String lastAccessTime;
    private String lastAccessDate;
    private List<Team> teams;
    private List<Fingerprint> fingerprints;
    private List<Nfc> nfcs;

    private List<Fingerprint> originalFingerprints;
    private List<Nfc> originalNfcs;

    public void saveOriginalNfcs()
    {
        originalNfcs = new ArrayList<Nfc>();
        for(Nfc nfc:getNfcs())
            originalNfcs.add(Cloner.deepClone(nfc));
    }

    public void saveOriginalFingerprints()
    {
        originalFingerprints = new ArrayList<Fingerprint>();
        for(Fingerprint fingerprint:getFingerprints())
            originalFingerprints.add(Cloner.deepClone(fingerprint));
    }

    public List<Fingerprint> getOriginalFingerprints()
    {
        if(originalFingerprints == null)
        {
            originalFingerprints = new ArrayList<Fingerprint>();
        }
        return originalFingerprints;
    }

    public List<Nfc> getOriginalNfcs()
    {
        if(originalNfcs == null)
        {
            originalNfcs = new ArrayList<Nfc>();
        }
        return originalNfcs;
    }

    public String getLastAccessDate() { return lastAccessDate;}
    public void setLastAccessDate(String lastAccessDate) {this.lastAccessDate = lastAccessDate;}
    public String getLastAccessTime() {return lastAccessTime;}
    public void setLastAccessTime(String lastAccessTime) {this.lastAccessTime = lastAccessTime;}

    public List<Team> getTeams()
    {
        if(teams == null)
        {
            teams = new ArrayList<Team>();
        }
        return teams;
    }

    public List<Nfc> getNfcs() {
        if(nfcs == null)
        {
            nfcs = new ArrayList<Nfc>();
        }
        return nfcs;
    }

    public List<Fingerprint> getFingerprints() {
        if(fingerprints == null)
        {
            fingerprints = new ArrayList<Fingerprint>();
        }
        return fingerprints;
    }

    public Fingerprint getFingerprint(int fingerBtnId) {
        List<Fingerprint> result = new ArrayList<Fingerprint>();
        for(Fingerprint fingerprint:getFingerprints())
        {
            if(fingerprint.match(fingerBtnId))
                return fingerprint;
        }
        return null;
    }

    public static Human CreateNewUser(String is_manager)
    {
        Human human = new Human();
        human.id = Human.NEW_DATA_ID;
        human.is_manager = is_manager;
        human.is_local = true;
        return human;
    }

    public boolean isManager()
    {
        return !TextUtils.isEmpty(is_manager);
    }

    public boolean hasDatFile()
    {
        int size = getDatPaths().size();
        return size != 0;
    }

    public List<String> getDatPaths()
    {
        Human human = this;

        List<String> result = new ArrayList<String>();

        for(Fingerprint fingerprint:human.getFingerprints())
        {
            String datPath = FileManager.getDatPath(human, Converter.englishToFingerBtnId(fingerprint.which));

            if(FileManager.exists(datPath))
                result.add(datPath);
        }

        return result;
    }
}
