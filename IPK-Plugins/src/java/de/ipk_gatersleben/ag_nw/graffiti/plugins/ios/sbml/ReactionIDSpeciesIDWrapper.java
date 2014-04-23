package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.sbml;

public class ReactionIDSpeciesIDWrapper {
	private String _reactionID;
	private String _speciesID;
	
	public ReactionIDSpeciesIDWrapper(String reactionID, String speciesID) {
		super();
		_reactionID = reactionID;
		_speciesID = speciesID;
	}
	
	public String getReactionID() {
		return _reactionID;
	}
	
	public void setReactionID(String _reactionID) {
		this._reactionID = _reactionID;
	}
	
	public String getSpeciesID() {
		return _speciesID;
	}
	
	public void setSpeciesID(String _speciesID) {
		this._speciesID = _speciesID;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_reactionID == null) ? 0 : _reactionID.hashCode());
		result = prime * result + ((_speciesID == null) ? 0 : _speciesID.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReactionIDSpeciesIDWrapper other = (ReactionIDSpeciesIDWrapper) obj;
		if (_reactionID == null) {
			if (other._reactionID != null)
				return false;
		} else if (!_reactionID.equals(other._reactionID))
			return false;
		if (_speciesID == null) {
			if (other._speciesID != null)
				return false;
		} else if (!_speciesID.equals(other._speciesID))
			return false;
		return true;
	}
	
}
