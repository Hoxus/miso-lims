package uk.ac.bbsrc.tgac.miso.core.store;

/**
 * Defines a DAO interface for storing KitDescriptors
 *
 * @author Michal Zak
 * @since 0.0.2
 */
import uk.ac.bbsrc.tgac.miso.core.data.KitDescriptor;
import uk.ac.bbsrc.tgac.miso.core.data.type.KitType;
import uk.ac.bbsrc.tgac.miso.core.data.type.PlatformType;

import java.io.IOException;
import java.util.List;

public interface KitDescriptorStore extends Store<KitDescriptor>{
    KitDescriptor getKitDescriptorById(long kitDescriptorId) throws IOException;
    KitDescriptor getKitDescriptorByPartNumber(String partNumber) throws IOException;
    List<KitDescriptor> listKitDescriptorsByManufacturer(String manufacturer) throws IOException;
    List<KitDescriptor> listKitDescriptorsByPlatform(PlatformType platformType) throws IOException;
    List<KitDescriptor> listKitDescriptorsByUnits(String units) throws IOException;
    List<KitDescriptor> listKitDescriptorsByType(KitType kitType) throws IOException;
}
