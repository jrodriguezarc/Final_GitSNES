/*
 * Copyright 2014 GitSNES Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package itcr.gitsnes;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import com.kanishka.virustotal.dto.FileScanReport;
import com.kanishka.virustotal.dto.ScanInfo;
import com.kanishka.virustotal.dto.VirusScanInfo;
import com.kanishka.virustotal.exception.APIKeyNotFoundException;
import com.kanishka.virustotal.exception.UnauthorizedAccessException;
import com.kanishka.virustotalv2.VirusTotalConfig;
import com.kanishka.virustotalv2.VirustotalPublicV2;
import com.kanishka.virustotalv2.VirustotalPublicV2Impl;

/* VirusTotal, a subsidiary of Google, is a free online service that
 * analyzes files and URLs enabling the identification of viruses, worms,
 * trojans and other kinds of malicious content detected by antivirus
 * engines and website scanners. At the same time, it may be used as a
 * means to detect false positives, i.e. innocuous resources detected as
 * malicious by one or more scanners.
 * VirusTotal’s mission is to help in improving the antivirus and security
 * industry and make the internet a safer place through the development
 * of free tools and services.VirusTotal's main characteristics are
 * highlighted below. */

 public class VirusScanner {



    /**
     * Print the report when this have been sent previously to scan() function
     * Input [file BUCKET URL]
     * */
    public String getUrlReport(String URI_FILE) {
        String str_report = "";
        try {
            VirusTotalConfig.getConfigInstance().setVirusTotalAPIKey(KeyStore.API_KEY);
            VirustotalPublicV2 virusTotalRef = new VirustotalPublicV2Impl();

            String urls[] = {URI_FILE};
            FileScanReport[] reports = virusTotalRef.getUrlScanReport(urls, false);

            for (FileScanReport report : reports) {
                if (report.getResponseCode() == 0) {
                    str_report = "Verbose Msg :\t" + report.getVerboseMessage();
                    continue;
                }
                str_report +=   "\nMD5 :\t" + report.getMd5()+
                                "\nPerma link :\t" + report.getPermalink()+
                                "\nResource :\t" + report.getResource()+
                                "\nScan Date :\t" + report.getScanDate()+
                                "\nScan Id :\t" + report.getScanId()+
                                "\nSHA1 :\t" + report.getSha1()+
                                "\nSHA256 :\t" + report.getSha256()+
                                "\nVerbose Msg :\t" + report.getVerboseMessage()+
                                "\nResponse Code :\t" + report.getResponseCode()+
                                "\nPositives :\t" + report.getPositives() +
                                "\nTotal :\t" + report.getTotal();

                Map<String, VirusScanInfo> scans = report.getScans();
                for (String key : scans.keySet()) {
                    VirusScanInfo virusInfo = scans.get(key);
                    str_report += "Scanner : " + key +
                    "\n\t\t Result : " + virusInfo.getResult()+
                    "\n\t\t Update : " + virusInfo.getUpdate()+
                    "\n\t\t Version :" + virusInfo.getVersion();
                }

                return str_report;
            }

        } catch (APIKeyNotFoundException ex) {
            Log.i(new KeyStore().TAG, "API Key not found! " + ex.getMessage());
        } catch (UnsupportedEncodingException ex) {
            Log.i(new KeyStore().TAG, "Unsupported Encoding Format!" + ex.getMessage());
        } catch (UnauthorizedAccessException ex) {
            Log.i(new KeyStore().TAG, "Invalid API Key " + ex.getMessage());
        } catch (Exception ex) {
            return  "Something Bad Happened! " + ex.getMessage();
        }

        return "Something Bad Happened! ";
    }



     /**
      * Function generate request to scan report
      * Input [file BUCKET URL]
      * */
     public void Scan(String URI_FILE) {
         try {
             VirusTotalConfig.getConfigInstance().setVirusTotalAPIKey(KeyStore.API_KEY);
             VirustotalPublicV2 virusTotalRef = new VirustotalPublicV2Impl();

             String urls[] = {URI_FILE};
             ScanInfo[] scanInfoArr = virusTotalRef.scanUrls(urls);

             for (ScanInfo scanInformation : scanInfoArr) {
                 System.out.println("___SCAN INFORMATION___");
                 System.out.println("MD5 :\t" + scanInformation.getMd5());
                 System.out.println("Perma Link :\t" + scanInformation.getPermalink());
                 System.out.println("Resource :\t" + scanInformation.getResource());
                 System.out.println("Scan Date :\t" + scanInformation.getScanDate());
                 System.out.println("Scan Id :\t" + scanInformation.getScanId());
                 System.out.println("SHA1 :\t" + scanInformation.getSha1());
                 System.out.println("SHA256 :\t" + scanInformation.getSha256());
                 System.out.println("Verbose Msg :\t" + scanInformation.getVerboseMessage());
                 System.out.println("Response Code :\t" + scanInformation.getResponseCode());
                 System.out.println("done.");
             }

         } catch (APIKeyNotFoundException ex) {
             System.err.println("API Key not found! " + ex.getMessage());
         } catch (UnsupportedEncodingException ex) {
             System.err.println("Unsupported Encoding Format!" + ex.getMessage());
         } catch (UnauthorizedAccessException ex) {
             System.err.println("Invalid API Key " + ex.getMessage());
         } catch (Exception ex) {
             System.err.println("Something Bad Happened! " + ex.getMessage());
         }
     }

}
