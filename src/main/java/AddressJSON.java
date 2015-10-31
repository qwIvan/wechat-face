class AddressJSON {

    /**
     * errNum : 0
     * retMsg : success
     * retData : {"address":"广东省江门市恩平市","sex":"M","birthday":"1995-07-15"}
     */

    private int errNum;
    private String retMsg;
    /**
     * address : 广东省江门市恩平市
     * sex : M
     * birthday : 1995-07-15
     */

    private RetDataEntity retData;

    public void setErrNum(int errNum) {
        this.errNum = errNum;
    }

    public void setRetMsg(String retMsg) {
        this.retMsg = retMsg;
    }

    public void setRetData(RetDataEntity retData) {
        this.retData = retData;
    }

    public int getErrNum() {
        return errNum;
    }

    public String getRetMsg() {
        return retMsg;
    }

    public RetDataEntity getRetData() {
        return retData;
    }

    public static class RetDataEntity {
        private String address;
        private String sex;
        private String birthday;

        public void setAddress(String address) {
            this.address = address;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public String getAddress() {
            return address;
        }

        public String getSex() {
            return sex;
        }

        public String getBirthday() {
            return birthday;
        }
    }
}