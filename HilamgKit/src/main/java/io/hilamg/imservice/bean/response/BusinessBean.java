package io.hilamg.imservice.bean.response;

public class BusinessBean {

    /**
     * groupId : 63
     * customer : {"id":"5","token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1IiwiaXNzIjoiZHVvZHVvIn0.Ixy83pkwa2METI7JnsKgLkgBbA1fo_MemgXb0w1N79o","rongToken":"123"}
     */

    private String groupId;
    private CustomerBean customer;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public CustomerBean getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerBean customer) {
        this.customer = customer;
    }

    public static class CustomerBean {
        /**
         * id : 5
         * token : eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1IiwiaXNzIjoiZHVvZHVvIn0.Ixy83pkwa2METI7JnsKgLkgBbA1fo_MemgXb0w1N79o
         * rongToken : 123
         */

        private String id;
        private String token;
        private String rongToken;
        private String nick;

        public String getNick() {
            return nick;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getRongToken() {
            return rongToken;
        }

        public void setRongToken(String rongToken) {
            this.rongToken = rongToken;
        }
    }
}
