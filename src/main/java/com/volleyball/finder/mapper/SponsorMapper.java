package com.volleyball.finder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volleyball.finder.dto.SponsorResponse;
import com.volleyball.finder.entity.Sponsors;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SponsorMapper extends BaseMapper<Sponsors> {
    @Select("""
                SELECT id, user_id, name, contact_email, phone, description, logo_url, website_url, is_active, use_line_pay
                FROM sponsors
            """)
    List<SponsorResponse> selectSponsorResponses();

    @Select("""
                SELECT id, user_id, name, contact_email, phone, description, logo_url, website_url, is_active, use_line_pay
                FROM sponsors
                WHERE id = #{id}
            """)
    SponsorResponse selectSponsorResponseById(@Param("id") Long id);
}
