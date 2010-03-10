package alma.scheduling.datamodel.observatory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class TelescopeEquipment extends AssemblyContainer {

    private String name;

    private Set<AssemblyContainer> assemblyGroups = new HashSet<AssemblyContainer>();
    
    public TelescopeEquipment() {
        super();
    }
    
    public TelescopeEquipment(String name, AssemblyGroupType type, Date commissionDate) {
        super(type, commissionDate);
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<AssemblyContainer> getAssemblyGroups() {
        return assemblyGroups;
    }

    public void setAssemblyGroups(Set<AssemblyContainer> assemblyGroups) {
        this.assemblyGroups = assemblyGroups;
    }
    
    public void addAssemblyGroup(AssemblyContainer group) {
        group.setParent(this);
        assemblyGroups.add(group);
    }
}
