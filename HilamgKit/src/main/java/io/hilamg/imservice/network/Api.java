package io.hilamg.imservice.network;

import io.hilamg.imservice.bean.response.AllGroupMembersResponse;
import io.hilamg.imservice.bean.response.BusinessBean;
import io.hilamg.imservice.bean.response.CommunityCultureResponse;
import io.hilamg.imservice.bean.response.CommunityInfoResponse;
import io.hilamg.imservice.bean.response.EditCommunityResponse;
import io.hilamg.imservice.bean.response.EditListCommunityCultureResponse;
import io.hilamg.imservice.bean.response.GetGroupPayInfoResponse;
import io.hilamg.imservice.bean.response.GetPaymentListBean;
import io.hilamg.imservice.bean.response.GetRedNewPersonInfoResponse;
import io.hilamg.imservice.bean.response.GetUpgradeGroupsResponnse;
import io.hilamg.imservice.bean.response.GroupManagementInfoBean;
import io.hilamg.imservice.bean.response.GroupResponse;
import io.hilamg.imservice.bean.response.LoginResponse;
import io.hilamg.imservice.bean.response.PermissionInfoBean;
import io.hilamg.imservice.bean.response.ReleaseRecord;
import io.hilamg.imservice.bean.response.ReleaseRecordDetails;
import io.hilamg.imservice.bean.response.base.BaseResponse;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface Api {

    @GET
    Call<ResponseBody> downloadFile(@Url String url);

    @POST("duoduo/customer/updateCustomerInfo")
    @FormUrlEncoded
    Observable<BaseResponse<LoginResponse>> updateUserInfo(@Field("customerInfo") String customerInfo);

    @POST("duoduo/group/getGroupMemByGroupId")
    @FormUrlEncoded
    Observable<BaseResponse<List<AllGroupMembersResponse>>> getGroupMemByGroupId(@Field("groupId") String groupId);

    @POST("duoduo/group/enterGroup")
    @FormUrlEncoded
    Observable<BaseResponse<CommunityCultureResponse>> enterGroup(
            @Field("groupId") String groupId,
            @Field("inviterId") String inviterId,
            @Field("customerIds") String customerIds
    );

    @POST("duoduo/group/getGroupByGroupId")
    @FormUrlEncoded
    Observable<BaseResponse<GroupResponse>> getGroupByGroupId(@Field("groupId") String groupId);

    @POST("duoduo/group/updateGroupInfo")
    @FormUrlEncoded
    Observable<BaseResponse<String>> updateGroupInfo(@Field("groupInfo") String groupInfo);

    @POST("duoduo/group/updateGroupOwner")
    @FormUrlEncoded
    Observable<BaseResponse<String>> updateGroupOwner(
            @Field("groupId") String groupId,
            @Field("customerId") String customerId
    );

    @POST("duoduo/group/getPermissionInfo")
    @FormUrlEncoded
    Observable<BaseResponse<List<PermissionInfoBean>>> getPermissionInfo(@Field("groupId") String groupId);

    @POST("duoduo/group/updatePermissionInfo")
    @FormUrlEncoded
    Observable<BaseResponse<String>> updatePermissionInfo(@Field("permissionInfo") String permissionInfo);

    @POST("duoduo/group/getGroupManagementInfo")
    @FormUrlEncoded
    Observable<BaseResponse<List<GroupManagementInfoBean>>> getGroupManagementInfo(@Field("groupId") String groupId, @Field("type") String type);

    @POST("duoduo/group/muteGroups")
    @FormUrlEncoded
    Observable<BaseResponse<String>> muteGroups(@Field("groupId") String groupId, @Field("type") String type);

    @POST("duoduo/group/groupOperation")
    @FormUrlEncoded
    Observable<BaseResponse<String>> groupOperation(@Field("groupId") String groupId, @Field("type") String type, @Field("source") String source);

    @POST("duoduo/group/kickOutORRemove")
    @FormUrlEncoded
    Observable<BaseResponse<String>> kickOutORRemove(@Field("groupId") String groupId, @Field("groupMembersId") String groupMembersId, @Field("type") String type);

    @POST("duoduo/group/muteMembers")
    @FormUrlEncoded
    Observable<BaseResponse<String>> muteMembers(@Field("groupId") String groupId, @Field("groupMembersId") String groupMembersId, @Field("type") String type);

    @POST("duoduo/group/getGroupPayInfo")
    @FormUrlEncoded
    Observable<BaseResponse<GetGroupPayInfoResponse>> getGroupPayInfo(@Field("groupId") String groupId);

    @POST("duoduo/group/groupPayInfo")
    @FormUrlEncoded
    Observable<BaseResponse<String>> groupPayInfo(@Field("groupId") String groupId, @Field("payFee") String payFee, @Field("isOpen") String isOpen, @Field("symbol") String symbol);

    @POST("duoduo/group/payToGroup")
    @FormUrlEncoded
    Observable<BaseResponse<CommunityCultureResponse>> payToGroup(@Field("groupId") String groupId, @Field("toCustomerId") String toCustomerId, @Field("payPwd") String payPwd, @Field("mot") String mot,
                                                                  @Field("symbol") String symbol);

    @POST("duoduo/group/getRedNewPersonInfo")
    @FormUrlEncoded
    Observable<BaseResponse<GetRedNewPersonInfoResponse>> getRedNewPersonInfo(@Field("groupId") String groupId);

    @POST("duoduo/group/upRedNewPersonInfo")
    @FormUrlEncoded
    Observable<BaseResponse<String>> upRedNewPersonInfo(@Field("data") String data);

    @POST("duoduo/redPackage/receiveNewPersonRedPackage")
    @FormUrlEncoded
    Observable<BaseResponse<GetRedNewPersonInfoResponse>> receiveNewPersonRedPackage(@Field("groupId") String groupId);

    @POST("duoduo/group/getUpgradeGroups")
    @FormUrlEncoded
    Observable<BaseResponse<GetUpgradeGroupsResponnse>> getUpgradeGroups(@Field("groupId") String groupId);

    @POST("duoduo/group/payToUpgradeGroup")
    @FormUrlEncoded
    Observable<BaseResponse<String>> payToUpgradeGroup(@Field("groupId") String groupId,
                                                       @Field("payPwd") String payPwd,
                                                       @Field("groupTag") String groupTag,
                                                       @Field("mot") String mot);

    @POST("duoduo/rongcloud/recallGroupMessage")
    @FormUrlEncoded
    Observable<BaseResponse<String>> recallGroupMessage(@Field("uId") String uId);

    @POST("duoduo/walletBalance/getPaymentList")
    Observable<BaseResponse<List<GetPaymentListBean>>> getPaymentList();

    @POST("duoduo/community/communityInfo")
    @FormUrlEncoded
    Observable<BaseResponse<CommunityInfoResponse>> communityInfo(@Field("groupId") String groupId);

    @POST("duoduo/community/communityCulture")
    @FormUrlEncoded
    Observable<BaseResponse<CommunityCultureResponse>> communityCulture(@Field("groupId") String id);

    @POST("duoduo/community/editCommunity")
    @FormUrlEncoded
    Observable<BaseResponse<EditCommunityResponse>> editCommunity(@Field("data") String data);

    @POST("duoduo/community/editListCommunityCulture")
    @FormUrlEncoded
    Observable<BaseResponse<EditListCommunityCultureResponse>> editListCommunityCulture(@Field("groupId") String groupId);

    @POST("duoduo/community/editCommunityWebSite")
    @FormUrlEncoded
    Observable<BaseResponse<String>> editCommunityWebSite(@Field("data") String data);

    @POST("duoduo/community/editCommunityVideo")
    @FormUrlEncoded
    Observable<BaseResponse<String>> editCommunityVideo(@Field("data") String data);

    @POST("duoduo/community/editCommunityApplication")
    @FormUrlEncoded
    Observable<BaseResponse<String>> editCommunityApplication(@Field("data") String data);

    @POST("duoduo/community/editCommunityFile")
    @FormUrlEncoded
    Observable<BaseResponse<String>> editCommunityFile(@Field("data") String data);

    @FormUrlEncoded
    @POST("duoduo/group/getReleaseRecord")
    Observable<BaseResponse<List<ReleaseRecord>>> releaseRecord(@Field("groupId") String groupId);

    @FormUrlEncoded
    @POST("duoduo/group/getReleaseRecordDetails")
    Observable<BaseResponse<ReleaseRecordDetails>> releaseRecordDetails(@Field("groupId") String groupId, @Field("symbol") String symbol, @Field("page") String page, @Field("offset") String offset);

    @FormUrlEncoded
    @POST("duoduo/saas/getBusiness")
    Observable<BaseResponse<BusinessBean>> getBusiness(@Field("appId") String appId, @Field("mobileOrEmail") String mobileOrEmail, @Field("sign") String sign);
}
