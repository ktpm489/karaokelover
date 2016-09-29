package vn.com.frankle.karaokelover.services.responses.zingmp3;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

/**
 * Created by duclm on 9/23/2016.
 */

@Generated("org.jsonschema2pojo")
public class ZingArtistDetail implements Parcelable {

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public ZingArtistDetail createFromParcel(Parcel source) {
            return new ZingArtistDetail(source);
        }

        @Override
        public ZingArtistDetail[] newArray(int size) {
            return new ZingArtistDetail[size];
        }
    };
    @SerializedName("artist_id")
    @Expose
    private int artistId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("alias")
    @Expose
    private String alias;
    @SerializedName("birthname")
    @Expose
    private String birthname;
    @SerializedName("birthday")
    @Expose
    private String birthday;
    @SerializedName("sex")
    @Expose
    private int sex;
    @SerializedName("genre_id")
    @Expose
    private String genreId;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("cover")
    @Expose
    private String cover;
    @SerializedName("cover3")
    @Expose
    private String cover3;
    @SerializedName("zme_acc")
    @Expose
    private String zmeAcc;
    @SerializedName("role")
    @Expose
    private String role;
    @SerializedName("website")
    @Expose
    private String website;
    @SerializedName("biography")
    @Expose
    private String biography;
    @SerializedName("agency_name")
    @Expose
    private String agencyName;
    @SerializedName("national_name")
    @Expose
    private String nationalName;
    @SerializedName("is_official")
    @Expose
    private int isOfficial;
    @SerializedName("year_active")
    @Expose
    private String yearActive;
    @SerializedName("status_id")
    @Expose
    private int statusId;
    @SerializedName("created_date")
    @Expose
    private int createdDate;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("genre_name")
    @Expose
    private String genreName;

    public ZingArtistDetail(Parcel in) {
        String[] data = new String[5];

        in.readStringArray(data);
        this.name = data[0];
        this.birthname = data[1];
        this.birthday = data[2];
        this.nationalName = data[3];
        this.biography = data[4];
    }

    /**
     * @return The artistId
     */
    public int getArtistId() {
        return artistId;
    }

    /**
     * @param artistId The artist_id
     */
    public void setArtistId(int artistId) {
        this.artistId = artistId;
    }

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias The alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return The birthname
     */
    public String getBirthname() {
        return birthname;
    }

    /**
     * @param birthname The birthname
     */
    public void setBirthname(String birthname) {
        this.birthname = birthname;
    }

    /**
     * @return The birthday
     */
    public String getBirthday() {
        return birthday;
    }

    /**
     * @param birthday The birthday
     */
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    /**
     * @return The sex
     */
    public int getSex() {
        return sex;
    }

    /**
     * @param sex The sex
     */
    public void setSex(int sex) {
        this.sex = sex;
    }

    /**
     * @return The genreId
     */
    public String getGenreId() {
        return genreId;
    }

    /**
     * @param genreId The genre_id
     */
    public void setGenreId(String genreId) {
        this.genreId = genreId;
    }

    /**
     * @return The avatar
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * @param avatar The avatar
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * @return The cover
     */
    public String getCover() {
        return cover;
    }

    /**
     * @param cover The cover
     */
    public void setCover(String cover) {
        this.cover = cover;
    }

    /**
     * @return The cover3
     */
    public String getCover3() {
        return cover3;
    }

    /**
     * @param cover3 The cover3
     */
    public void setCover3(String cover3) {
        this.cover3 = cover3;
    }

    /**
     * @return The zmeAcc
     */
    public String getZmeAcc() {
        return zmeAcc;
    }

    /**
     * @param zmeAcc The zme_acc
     */
    public void setZmeAcc(String zmeAcc) {
        this.zmeAcc = zmeAcc;
    }

    /**
     * @return The role
     */
    public String getRole() {
        return role;
    }

    /**
     * @param role The role
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * @return The website
     */
    public String getWebsite() {
        return website;
    }

    /**
     * @param website The website
     */
    public void setWebsite(String website) {
        this.website = website;
    }

    /**
     * @return The biography
     */
    public String getBiography() {
        return biography;
    }

    /**
     * @param biography The biography
     */
    public void setBiography(String biography) {
        this.biography = biography;
    }

    /**
     * @return The agencyName
     */
    public String getAgencyName() {
        return agencyName;
    }

    /**
     * @param agencyName The agency_name
     */
    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    /**
     * @return The nationalName
     */
    public String getNationalName() {
        return nationalName;
    }

    /**
     * @param nationalName The national_name
     */
    public void setNationalName(String nationalName) {
        this.nationalName = nationalName;
    }

    /**
     * @return The isOfficial
     */
    public int getIsOfficial() {
        return isOfficial;
    }

    /**
     * @param isOfficial The is_official
     */
    public void setIsOfficial(int isOfficial) {
        this.isOfficial = isOfficial;
    }

    /**
     * @return The yearActive
     */
    public String getYearActive() {
        return yearActive;
    }

    /**
     * @param yearActive The year_active
     */
    public void setYearActive(String yearActive) {
        this.yearActive = yearActive;
    }

    /**
     * @return The statusId
     */
    public int getStatusId() {
        return statusId;
    }

    /**
     * @param statusId The status_id
     */
    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    /**
     * @return The createdDate
     */
    public int getCreatedDate() {
        return createdDate;
    }

    /**
     * @param createdDate The created_date
     */
    public void setCreatedDate(int createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * @return The link
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link The link
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * @return The genreName
     */
    public String getGenreName() {
        return genreName;
    }

    /**
     * @param genreName The genre_name
     */
    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.name,
                this.birthname,
                this.birthday,
                this.nationalName,
                this.biography});
    }
}
