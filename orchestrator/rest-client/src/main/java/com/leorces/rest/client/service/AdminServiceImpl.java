package com.leorces.rest.client.service;

import com.leorces.api.AdminService;
import com.leorces.rest.client.client.AdminClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service("leorcesAdminService")
public class AdminServiceImpl implements AdminService {

    private final AdminClient adminClient;

    @Override
    public void doCompaction() {
        adminClient.doCompaction();
    }

}
