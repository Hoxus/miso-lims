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

package uk.ac.bbsrc.tgac.miso.core.data;

import com.eaglegenomics.simlims.core.SecurityProfile;
import com.eaglegenomics.simlims.core.User;
import org.w3c.dom.Document;
import uk.ac.bbsrc.tgac.miso.core.data.impl.PlatformImpl;
import uk.ac.bbsrc.tgac.miso.core.data.type.KitType;
import uk.ac.bbsrc.tgac.miso.core.security.SecurableByProfile;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Skeleton implementation of an Experiment
 *
 * @author Rob Davey
 * @since 0.0.2
 */
public abstract class AbstractExperiment implements Experiment {
  public static final Long UNSAVED_ID = 0L;

  @OneToOne(cascade = CascadeType.ALL)
  private SecurityProfile securityProfile;

  @Transient
  public Document submissionDocument;

  @ManyToOne(targetEntity = AbstractStudy.class, cascade = CascadeType.ALL)
  private Study study = null;

  //defines a pool on which this experiment will operate. This contains one or more dilutions of a sample
  private Pool pool;

  //defines the parent run which processes this experiment
  private Run run;

  private String title;
  private String name;
  private String description;
  private String alias;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long experimentId = AbstractExperiment.UNSAVED_ID;
  private String accession;

  @OneToOne(targetEntity = PlatformImpl.class, cascade = CascadeType.ALL)
  private Platform platform;
  
  private Collection<KitComponent> kitComponents = new HashSet<KitComponent>();

  public Study getStudy() {
    return study;
  }

  public void setStudy(Study study) {
    this.study = study;
  }

  @Deprecated
  public Long getExperimentId() {
    return experimentId;
  }

  @Deprecated
  public void setExperimentId(Long experimentId) {
    this.experimentId = experimentId;
  }

  @Override
  public long getId() {
    return experimentId;
  }

  public void setId(long id) {
    this.experimentId = id;
  }

  public String getAccession() {
    return accession;
  }

  public void setAccession(String accession) {
    this.accession = accession;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }  

  public Platform getPlatform() {
    return platform;
  }

  public void setPlatform(Platform platform) {
    this.platform = platform;
  }

/*  public void addRun(Run r) throws MalformedRunException {
    try {
      //do experiment validation
      r.addExperiment(this);

      //propagate security profiles down the hierarchy
      r.setSecurityProfile(this.securityProfile);

      //add
      this.runs.add(r);
    }
    catch (MalformedExperimentException e) {
      e.printStackTrace();
    }
  }

  public Collection<Run> getRuns() {
    return runs;
  }*/

  public Run getRun() {
    return run;
  }

  public void setRun(Run run) {
    this.run = run;
  }

/*  public void addSample(Sample s) throws MalformedSampleException {
    //do experiment validation
    try {
      s.addExperiment(this);

      //propagate security profiles down the hierarchy
      s.setSecurityProfile(this.securityProfile);

      //add
      this.samples.add(s);
    }
    catch (MalformedExperimentException e) {
      e.printStackTrace();
    }
  }

  public Collection<Sample> getSamples() {
    return samples;
  }
*/

  public Pool getPool() {
    return pool;
  }

  public void setPool(Pool pool) {
    this.pool = pool;
  }

  public Collection<KitComponent> getKitComponents() {
    return kitComponents;
  }

  public Collection<KitComponent> getKitsByKitType(KitType kitType) {
    ArrayList<KitComponent> ks = new ArrayList<KitComponent>();
    for (KitComponent k : kitComponents) {
      if (k.getKitComponentDescriptor().getKitDescriptor().getKitType().equals(kitType)) {
        ks.add(k);
      }
    }
    Collections.sort(ks);
    return ks;
  }

  public void setKitComponents(Collection<KitComponent> kitComponents) {
    this.kitComponents = kitComponents;
  }

  public void addKit(KitComponent kitComponent) {
    this.kitComponents.add(kitComponent);
  }

/*
  public Document getSubmissionData() {
    return submissionDocument;
  }

  public void accept(SubmittableVisitor v) {
    v.visit(this);
  }  
*/

  public boolean isDeletable() {
    return getId() != AbstractExperiment.UNSAVED_ID;
    /*&&
           getKitComponents().isEmpty() &&
           getPool() == null;
              */
  }

  public SecurityProfile getSecurityProfile() {
    return securityProfile;
  }

  public void setSecurityProfile(SecurityProfile profile) {
    this.securityProfile = profile;
  }

  public void inheritPermissions(SecurableByProfile parent) throws SecurityException {
    if (parent.getSecurityProfile().getOwner() != null) {
      setSecurityProfile(parent.getSecurityProfile());
    }
    else {
      throw new SecurityException("Cannot inherit permissions when parent object owner is not set!");
    }
  }

  public boolean userCanRead(User user) {
    return securityProfile.userCanRead(user);
  }

  public boolean userCanWrite(User user) {
    return securityProfile.userCanWrite(user);
  }

  public abstract void buildSubmission();  

  /**
   * Equivalency is based on getProjectId() if set, otherwise on name,
   * description and creation date.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (obj == this)
      return true;
    if (!(obj instanceof Experiment))
      return false;
    Experiment them = (Experiment) obj;
    // If not saved, then compare resolved actual objects. Otherwise
    // just compare IDs.
    if (getId() == AbstractExperiment.UNSAVED_ID
        || them.getId() == AbstractExperiment.UNSAVED_ID) {
      if (getName() != null && them.getName() != null) {
        return getName().equals(them.getName());
      }
      else {
        return getAlias().equals(them.getAlias());
      }
    }
    else {
      return getId() == them.getId();
    }
  }

  @Override
  public int hashCode() {
    if (getId() != AbstractExperiment.UNSAVED_ID) {
      return (int)getId();
    }
    else {
      final int PRIME = 37;
      int hashcode = 1;
      if (getName() != null) hashcode = PRIME * hashcode + getName().hashCode();
      if (getAlias() != null) hashcode = 37 * hashcode + getAlias().hashCode();
      return hashcode;
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getAccession());
    sb.append(" : ");
    sb.append(getTitle());
    sb.append(" : ");
    sb.append(getName());
    sb.append(" : ");
    sb.append(getDescription());
    sb.append(" : ");
    sb.append(getPool());
    sb.append(" : ");
    if (getPlatform() != null) {
      sb.append(getPlatform().getInstrumentModel());
      sb.append(" : ");
    }

    return sb.toString();
  }

  @Override
  public int compareTo(Object o) {
    Experiment t = (Experiment)o;
    if (getId() < t.getId()) return -1;
    if (getId() > t.getId()) return 1;
    return 0;
  }
}
