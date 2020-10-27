package com.example.demo;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.cvm.v20170312.CvmClient;
import com.tencentcloudapi.cvm.v20170312.models.DescribeInstancesRequest;
import com.tencentcloudapi.cvm.v20170312.models.DescribeInstancesResponse;
import com.tencentcloudapi.live.v20180801.LiveClient;
import com.tencentcloudapi.live.v20180801.models.DescribeLogDownloadListRequest;
import com.tencentcloudapi.live.v20180801.models.DescribeLogDownloadListResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *  腾讯云api调用测试
 *  1.需要先引入tencentcloud-sdk-java依赖
 *  2.SecretId、SecretKey可以从腾讯云后台获取 https://console.cloud.tencent.com/cam/capi
 */
@RequestMapping("/tencent/api")
public class TencentCloudApiTest {

    public void test(){
        try {
            Credential cred = new Credential("AKIDQbV9ZkDZCk24tRlqJkcEafSOIp79QsRm", "FJ8g0EI5ewztWtAE2e4GCrEZnkV1KNTH");
            CvmClient client = new CvmClient(cred, "ap-guangzhou");
            DescribeInstancesRequest req = new DescribeInstancesRequest();
            DescribeInstancesResponse resp = client.DescribeInstances(req);
            System.out.println("CVM======= "+DescribeInstancesResponse.toJsonString(resp));
        } catch (TencentCloudSDKException e) {
            System.out.println("错误信息: " + e.toString());
        }
    }


    public void test2(){
        try {
            Credential cred = new Credential("AKIDQbV9ZkDZCk24tRlqJkcEafSOIp79QsRm", "FJ8g0EI5ewztWtAE2e4GCrEZnkV1KNTH");
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("live.tencentcloudapi.com");

            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            LiveClient client = new LiveClient(cred, "ap-guangzhou", clientProfile);
            DescribeLogDownloadListRequest req = new DescribeLogDownloadListRequest();
            String[] playDomains1 = {"116966.livepush.myqcloud.com"};
            req.setPlayDomains(playDomains1);
            req.setStartTime("2020-10-20 10:10:10");
            req.setEndTime("2020-10-22 10:10:10");
            DescribeLogDownloadListResponse resp = client.DescribeLogDownloadList(req);

            System.out.println(DescribeLogDownloadListResponse.toJsonString(resp));
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
    }

    @GetMapping("/open/callback")
    public void test3(){
        System.out.println("开播回调");
    }
}
