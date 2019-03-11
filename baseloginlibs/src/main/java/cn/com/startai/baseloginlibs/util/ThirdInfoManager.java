package cn.com.startai.baseloginlibs.util;

/**
 * Created by Robin on 2019/3/1.
 * qq: 419109715 彬影
 */

public class ThirdInfoManager {
    private static final ThirdInfoManager ourInstance = new ThirdInfoManager();

    public static ThirdInfoManager getInstance() {
        return ourInstance;
    }

    private ThirdInfoManager() {
    }

    private TwitterDeveloperInfo developerInfo;


    public void initTwitterDeveloper(TwitterDeveloperInfo developerInfo){
        this.developerInfo = developerInfo;
    }


    public TwitterDeveloperInfo getTwitterDeveloperInfo(){

        return  this.developerInfo;
    }

    public static class TwitterDeveloperInfo{

        private String twitterApiKey;
        private String twitterApiSecrite;

        public TwitterDeveloperInfo() {
        }

        public TwitterDeveloperInfo(String twitterApiKey, String twitterApiSecrite) {
            this.twitterApiKey = twitterApiKey;
            this.twitterApiSecrite = twitterApiSecrite;
        }

        @Override
        public String toString() {
            return "TwitterDeveloperInfo{" +
                    "twitterApiKey='" + twitterApiKey + '\'' +
                    ", twitterApiSecrite='" + twitterApiSecrite + '\'' +
                    '}';
        }

        public String getTwitterApiKey() {

            return twitterApiKey;
        }

        public void setTwitterApiKey(String twitterApiKey) {
            this.twitterApiKey = twitterApiKey;
        }

        public String getTwitterApiSecrite() {
            return twitterApiSecrite;
        }

        public void setTwitterApiSecrite(String twitterApiSecrite) {
            this.twitterApiSecrite = twitterApiSecrite;
        }
    }


}
