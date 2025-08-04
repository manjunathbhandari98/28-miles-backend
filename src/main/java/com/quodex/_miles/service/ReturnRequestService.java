package com.quodex._miles.service;

import com.quodex._miles.constant.ReturnStatus;
import com.quodex._miles.io.ReturnProcessRequest;
import com.quodex._miles.io.ReturnRequest;
import com.quodex._miles.io.ReturnResponse;

import java.util.List;

public interface ReturnRequestService {
    ReturnResponse addReturnRequest(ReturnRequest request);
    ReturnResponse processReturnRequest(String requestId, ReturnProcessRequest dto);
    List<ReturnResponse>  getReturnsByUser(String userId);
    ReturnResponse getReturnByReturnId(String returnId);
    void deleteReturnRequest(String returnId);
    ReturnResponse getReturnByOrder(String orderId);
}
