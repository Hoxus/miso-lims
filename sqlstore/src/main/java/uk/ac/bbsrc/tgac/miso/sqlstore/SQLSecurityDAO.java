/*
 * Copyright (c) 2012. The Genome Analysis Centre, Norwich, UK
 * MISO project contacts: Robert Davey, Mario Caccamo @ TGAC
 * *********************************************************************
 *
 * This file is part of MISO.
 *
 * MISO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MISO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MISO.  If not, see <http://www.gnu.org/licenses/>.
 *
 * *********************************************************************
 */

package uk.ac.bbsrc.tgac.miso.sqlstore;

import static uk.ac.bbsrc.tgac.miso.core.util.LimsUtils.isStringEmptyOrNull;

import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;

import com.eaglegenomics.simlims.core.Group;
import com.eaglegenomics.simlims.core.SecurityProfile;
import com.eaglegenomics.simlims.core.User;
import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;
import com.googlecode.ehcache.annotations.Property;
import com.googlecode.ehcache.annotations.TriggersRemove;

import uk.ac.bbsrc.tgac.miso.core.data.impl.UserImpl;
import uk.ac.bbsrc.tgac.miso.core.security.PasswordCodecService;
import uk.ac.bbsrc.tgac.miso.core.store.SecurityStore;
import uk.ac.bbsrc.tgac.miso.core.store.Store;
import uk.ac.bbsrc.tgac.miso.core.util.CoverageIgnore;
import uk.ac.bbsrc.tgac.miso.core.util.LimsUtils;
import uk.ac.bbsrc.tgac.miso.sqlstore.cache.CacheAwareRowMapper;
import uk.ac.bbsrc.tgac.miso.sqlstore.util.DbUtils;

/**
 * uk.ac.bbsrc.tgac.miso.sqlstore
 * <p/>
 * Info
 *
 * @author Rob Davey
 * @since 0.0.2
 */
@Transactional(rollbackFor = Exception.class)
public class SQLSecurityDAO implements SecurityStore {
  private static final String USER_TABLE_NAME = "User";
  private static final String GROUP_TABLE_NAME = "_Group";
  
  public static final String USERS_SELECT = "SELECT userId, active, admin, external, fullName, internal, loginName, roles, password, email "
      + "FROM " + USER_TABLE_NAME;

  public static final String USER_SELECT_BY_ID = USERS_SELECT + " WHERE userId = ?";

  public static final String USER_SELECT_BY_IDS = USERS_SELECT + " WHERE userId IN (:ids)";

  public static final String USER_SELECT_BY_LOGIN_NAME = USERS_SELECT + " WHERE loginName = ?";

  public static final String USER_SELECT_BY_EMAIL = USERS_SELECT + " WHERE email = ?";

  public static final String USERS_SELECT_BY_GROUP_ID = "SELECT u.userId, u.active, u.admin, u.external, u.fullName, u.internal, u.loginName, u.roles, u.password, u.email "
      + "FROM User u, User_Group ug " + "WHERE u.userId=ug.users_userId " + "AND ug.groups_groupId=?";

  public static final String USERS_SELECT_BY_GROUP_NAME = "SELECT u.* FROM User u "
      + "LEFT JOIN User_Group ug ON ug.users_userId = u.userId " + "LEFT JOIN _Group g ON ug.groups_groupId = g.groupId "
      + "WHERE g.name = ?";

  public static final String USER_UPDATE = "UPDATE " + USER_TABLE_NAME + " SET active=:active, admin=:admin, external=:external, fullName=:fullName, "
      + "internal=:internal, loginName=:loginName, roles=:roles, password=:password, email=:email " + "WHERE userId=:userId";

  public static final String GROUPS_SELECT = "SELECT groupId, description, name " + "FROM " + GROUP_TABLE_NAME;

  public static final String GROUP_SELECT_BY_ID = GROUPS_SELECT + " WHERE groupId = ?";

  public static final String GROUP_SELECT_BY_IDS = GROUPS_SELECT + " WHERE groupId IN (:ids)";

  public static final String GROUP_SELECT_BY_NAME = GROUPS_SELECT + " WHERE name = ?";

  public static final String GROUPS_SELECT_BY_USER_ID = "SELECT g.groupId, g.name, g.description " + "FROM _Group g, User_Group ug "
      + "WHERE g.groupId=ug.groups_groupId " + "AND ug.users_userId=?";

  public static final String GROUP_UPDATE = "UPDATE " + GROUP_TABLE_NAME + " SET name=:name, description=:description " + "WHERE groupId=:groupId";

  public static final String USER_GROUP_DELETE_BY_USER_ID = "DELETE FROM User_Group " + "WHERE users_userId=:userId";

  protected static final Logger log = LoggerFactory.getLogger(SQLSecurityDAO.class);

  @Autowired
  private PasswordCodecService passwordCodecService;

  private Store<SecurityProfile> securityProfileDAO;
  private JdbcTemplate template;

  @Autowired
  private CacheManager cacheManager;

  @CoverageIgnore
  public void setCacheManager(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @CoverageIgnore
  public void setSecurityProfileDAO(Store<SecurityProfile> securityProfileDAO) {
    this.securityProfileDAO = securityProfileDAO;
  }

  @CoverageIgnore
  public JdbcTemplate getJdbcTemplate() {
    return template;
  }

  @CoverageIgnore
  public void setJdbcTemplate(JdbcTemplate template) {
    this.template = template;
  }

  @CoverageIgnore
  public PasswordCodecService getPasswordCodecService() {
    return passwordCodecService;
  }

  @CoverageIgnore
  public void setPasswordCodecService(PasswordCodecService passwordCodecService) {
    this.passwordCodecService = passwordCodecService;
  }

  @Override
  @TriggersRemove(cacheName = { "userCache",
      "lazyUserCache" }, keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
          @Property(name = "includeMethod", value = "false"), @Property(name = "includeParameterTypes", value = "false") }) )
  public long saveUser(User user) throws IOException {
    Blob roleBlob = null;
    if (user.getRoles() != null) {
      List<String> roles = new ArrayList<String>(Arrays.asList(user.getRoles()));
      if (user.isExternal() && !roles.contains("ROLE_EXTERNAL")) roles.add("ROLE_EXTERNAL");
      if (user.isInternal() && !roles.contains("ROLE_INTERNAL")) roles.add("ROLE_INTERNAL");
      if (user.isAdmin() && !roles.contains("ROLE_ADMIN")) roles.add("ROLE_ADMIN");
      user.setRoles(roles.toArray(new String[user.getRoles().length]));

      try {
        if (user.getRoles().length > 0) {
          byte[] rbytes = LimsUtils.join(user.getRoles(), ",").getBytes();
          roleBlob = new SerialBlob(rbytes);
        }
      } catch (SerialException e) {
        log.error("save user", e);
      } catch (SQLException e) {
        log.error("save user", e);
      }
    }

    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("active", user.isActive());
    params.addValue("admin", user.isAdmin());
    params.addValue("external", user.isExternal());
    params.addValue("fullName", user.getFullName());
    params.addValue("internal", user.isInternal());
    params.addValue("loginName", user.getLoginName());
    params.addValue("roles", roleBlob);
    params.addValue("email", user.getEmail());

    if (user.getUserId() != UserImpl.UNSAVED_ID) {
      User existingUser = getUserById(user.getUserId());
      if (existingUser != null) {
        // if the user already exists, but no password has been set, grab the existing one
        // this is probably due to an admin change of user properties, but not a password change
        if (!isStringEmptyOrNull(user.getPassword())) {
          if (!isStringEmptyOrNull(existingUser.getPassword())) {
            user.setPassword(existingUser.getPassword());
            params.addValue("password", user.getPassword());
          }
        } else {
          // if the user already exists, check to see if the passwords match. if not, update.
          if (passwordCodecService != null) {
            if (!passwordCodecService.getEncoder().isPasswordValid(existingUser.getPassword(), user.getPassword(), null)) {
              params.addValue("password", passwordCodecService.encrypt(user.getPassword()));
            } else {
              params.addValue("password", user.getPassword());
            }
          } else {
            log.warn("No PasswordCodecService has been wired to this SQLSecurityDAO. This means your passwords may be being "
                + "stored in plaintext, or being encrypted by a downstream encoder. Please specify a PasswordCodecService "
                + "in your Spring config and (auto)wire it to this DAO, if required.");
            params.addValue("password", user.getPassword());
          }
        }
      } else {
        throw new IOException("Cannot find existing user with specified ID");
      }
    } else {
      // if the user doesn't exist, encrypt
      if (passwordCodecService != null) {
        params.addValue("password", passwordCodecService.encrypt(user.getPassword()));
      } else {
        log.warn("No PasswordCodecService has been wired to this SQLSecurityDAO. This means your passwords may be being "
            + "stored in plaintext, or being encrypted by a downstream encoder. Please specify a PasswordCodecService "
            + "in your Spring config and (auto)wire it to this DAO, if required.");
        params.addValue("password", user.getPassword());
      }
    }

    if (user.getUserId() == UserImpl.UNSAVED_ID) {
      SimpleJdbcInsert insert = new SimpleJdbcInsert(template).withTableName("User").usingGeneratedKeyColumns("userId");
      Number newId = insert.executeAndReturnKey(params);
      user.setUserId(newId.longValue());
    } else {
      params.addValue("userId", user.getUserId());
      NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(template);
      namedTemplate.update(USER_UPDATE, params);
    }

    // sort User_Group

    // delete existing joins
    MapSqlParameterSource delparams = new MapSqlParameterSource();
    delparams.addValue("userId", user.getUserId());
    NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(template);
    namedTemplate.update(USER_GROUP_DELETE_BY_USER_ID, delparams);

    if (user.getGroups() != null && !user.getGroups().isEmpty()) {
      SimpleJdbcInsert eInsert = new SimpleJdbcInsert(template).withTableName("User_Group");
      for (Group g : user.getGroups()) {
        MapSqlParameterSource ugParams = new MapSqlParameterSource();
        ugParams.addValue("users_userId", user.getUserId());
        ugParams.addValue("groups_groupId", g.getGroupId());

        eInsert.execute(ugParams);
      }
    }

    if (cacheManager != null) {
      DbUtils.updateCaches(cacheManager.getCache("userCache"), user.getUserId());
    }

    return user.getUserId();
  }

  @Override
  @Cacheable(cacheName = "userCache", keyGenerator = @KeyGenerator(name = "HashCodeCacheKeyGenerator", properties = {
      @Property(name = "includeMethod", value = "false"), @Property(name = "includeParameterTypes", value = "false") }) )
  public User getUserById(Long userId) throws IOException {
    List<User> results = template.query(USER_SELECT_BY_ID, new Object[] { userId }, new UserMapper());
    User u = results.size() > 0 ? (User) results.get(0) : null;
    return u;
  }

  @Override
  public User getUserByLoginName(String loginName) throws IOException {
    List<User> results = template.query(USER_SELECT_BY_LOGIN_NAME, new Object[] { loginName }, new UserMapper());
    User u = results.size() > 0 ? (User) results.get(0) : null;
    return u;
  }

  @Override
  public User getUserByEmail(String email) throws IOException {
    List<User> results = template.query(USER_SELECT_BY_EMAIL, new Object[] { email }, new UserMapper());
    User u = results.size() > 0 ? (User) results.get(0) : null;
    return u;
  }

  @Override
  public Collection<User> listAllUsers() throws IOException {
    return template.query(USERS_SELECT, new UserMapper());
  }

  @Override
  public Collection<User> listUsersByIds(Collection<Long> userIds) throws IOException {
    if (userIds.size() > 0) {
      Set<User> results = new HashSet<User>();
      for (long userId : userIds) {
        User u = getUserById(userId);
        if (u != null) results.add(u);
      }
      return results;
    }
    return Collections.emptySet();
  }

  @Override
  public Collection<User> listUsersByGroupName(String name) throws IOException {
    if (name == null) throw new NullPointerException("Can not search by null group name");
    return template.query(USERS_SELECT_BY_GROUP_NAME, new Object[] { name }, new UserMapper(true));
  }

  @Override
  public long saveGroup(Group group) throws IOException {
    MapSqlParameterSource params = new MapSqlParameterSource();
    params.addValue("name", group.getName());
    params.addValue("description", group.getDescription());

    if (group.getGroupId() == Group.UNSAVED_ID) {
      SimpleJdbcInsert insert = new SimpleJdbcInsert(template).withTableName("_Group").usingGeneratedKeyColumns("groupId");
      Number newId = insert.executeAndReturnKey(params);
      group.setGroupId(newId.longValue());
    } else {
      params.addValue("groupId", group.getGroupId());
      NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(template);
      namedTemplate.update(GROUP_UPDATE, params);
    }

    return group.getGroupId();
  }

  @Override
  public Group getGroupById(Long groupId) throws IOException {
    if (groupId == null) throw new NullPointerException("Cannot search for null groupId");
    List<Group> results = template.query(GROUP_SELECT_BY_ID, new Object[] { groupId }, new GroupMapper());
    Group g = results.size() > 0 ? (Group) results.get(0) : null;
    return g;
  }

  @Override
  public Group getGroupByName(String groupName) throws IOException {
    if (groupName == null) throw new NullPointerException("Cannot search for null group name");
    List<Group> results = template.query(GROUP_SELECT_BY_NAME, new Object[] { groupName }, new GroupMapper());
    Group g = results.size() > 0 ? (Group) results.get(0) : null;
    return g;
  }

  public Collection<Group> listGroupsByUserId(Long userId) throws IOException {
    return template.query(GROUPS_SELECT_BY_USER_ID, new Object[] { userId }, new GroupMapper());
  }

  @Override
  public Collection<Group> listAllGroups() throws IOException {
    return template.query(GROUPS_SELECT, new GroupMapper());
  }

  @Override
  public Collection<Group> listGroupsByIds(Collection<Long> groupIds) throws IOException {
    if (groupIds.size() > 0) {
      NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(template);
      MapSqlParameterSource params = new MapSqlParameterSource();
      params.addValue("ids", groupIds);
      return namedTemplate.query(GROUP_SELECT_BY_IDS, params, new GroupMapper());
    }
    return Collections.emptySet();
  }

  @Override
  public SecurityProfile getSecurityProfileById(Long profileId) throws IOException {
    return securityProfileDAO.get(profileId);
  }

  public class UserMapper extends CacheAwareRowMapper<User> {
    public UserMapper() {
      super(User.class);
    }

    public UserMapper(boolean lazy) {
      super(User.class, lazy);
    }

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
      long id = rs.getLong("userId");

      if (isCacheEnabled() && lookupCache(cacheManager) != null) {
        Element element;
        if ((element = lookupCache(cacheManager).get(DbUtils.hashCodeCacheKeyFor(id))) != null) {
          log.debug("Cache hit on map for User " + id);
          return (User) element.getObjectValue();
        }
      }

      User user = new UserImpl();
      user.setUserId(id);
      user.setActive(rs.getBoolean("active"));
      user.setAdmin(rs.getBoolean("admin"));
      user.setExternal(rs.getBoolean("external"));
      user.setFullName(rs.getString("fullName"));
      user.setInternal(rs.getBoolean("internal"));
      user.setLoginName(rs.getString("loginName"));
      user.setPassword(rs.getString("password"));
      user.setEmail(rs.getString("email"));

      try {
        Blob roleblob = rs.getBlob("roles");
        if (roleblob != null) {
          if (roleblob.length() > 0) {
            byte[] rbytes = roleblob.getBytes(1, (int) roleblob.length());
            String s1 = new String(rbytes);
            String[] roles = s1.split(",");
            user.setRoles(roles);
          }
        }
        if (!isLazy()) {
          user.setGroups(listGroupsByUserId(id));
        }
      } catch (IOException e) {
        log.error("user row mapper", e);
      }

      if (isCacheEnabled() && lookupCache(cacheManager) != null) {
        lookupCache(cacheManager).put(new Element(DbUtils.hashCodeCacheKeyFor(id), user));
      }

      return user;
    }
  }

  public class GroupMapper implements RowMapper<Group> {
    @Override
    public Group mapRow(ResultSet rs, int rowNum) throws SQLException {
      Group g = new Group();
      g.setGroupId(rs.getLong("groupId"));
      g.setName(rs.getString("name"));
      g.setDescription(rs.getString("description"));

      try {
        g.setUsers(listUsersByGroupName(g.getName()));
      } catch (IOException e) {
        log.error("group row mapper", e);
      }

      return g;
    }
  }

  @Override
  public Map<String, Integer> getUserColumnSizes() throws IOException {
    return DbUtils.getColumnSizes(template, USER_TABLE_NAME);
  }

  @Override
  public Map<String, Integer> getGroupColumnSizes() throws IOException {
    return DbUtils.getColumnSizes(template, GROUP_TABLE_NAME);
  }
}
